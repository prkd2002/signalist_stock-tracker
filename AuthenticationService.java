package com.rothenberger.roapp_backend.services;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javax.management.relation.RoleNotFoundException;
import javax.naming.AuthenticationException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.rothenberger.roapp_backend.dtos.LoginUserDto;
import com.rothenberger.roapp_backend.dtos.RegisterUserDto;
import com.rothenberger.roapp_backend.dtos.RfidLoginUserDto;
import com.rothenberger.roapp_backend.entities.Linie;
import com.rothenberger.roapp_backend.entities.LinieAccessConfig;
import com.rothenberger.roapp_backend.entities.Role;
import com.rothenberger.roapp_backend.entities.Role_Enum;
import com.rothenberger.roapp_backend.entities.User;
import com.rothenberger.roapp_backend.exceptions.AccessExpiredException;
import com.rothenberger.roapp_backend.exceptions.AccessLinieDeniedException;
import com.rothenberger.roapp_backend.exceptions.InvalidApiKeyException;
import com.rothenberger.roapp_backend.exceptions.LinieNotFoundException;
import com.rothenberger.roapp_backend.exceptions.ResourceNotFoundException;
import com.rothenberger.roapp_backend.exceptions.RfidNotFoundException;
import com.rothenberger.roapp_backend.repository.LinieRepository;
import com.rothenberger.roapp_backend.repository.RoleRepository;
import com.rothenberger.roapp_backend.repository.UserRepository;
import com.rothenberger.roapp_backend.utils.ApiKeyAuthentication;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthenticationService {

    @Value("${app.security.api-key}")
    private String apiKey;

    @Value("${app.security.api-key-header}")
    private String apiKeyHeaderName;

    private static AuthenticationService instance;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final LinieRepository linieRepository;
    private final ModelMapper modelMapper;

    /**
     * Initialize static instance for API key access
     */
    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * Get the configured API key
     * 
     * @return The API key
     */
    public static String getApiKey() {
        return instance.apiKey;
    }

    /*
     * Get the configured API kex header name
     * 
     * @return the API key header name
     */
    public static String getApiKeyHeaderName() {
        return instance.apiKeyHeaderName;
    }

    /**
     * Authenticate API key form the request
     * 
     * @param request HttpServletRequest containing the API key
     * @return Authentication object if API key is valid
     * @throws BadCredendentialsException if API key is invalid
     */
    public static Authentication getAuthentication(HttpServletRequest request) throws InvalidApiKeyException {
        String requestApiKey = request.getHeader(getApiKeyHeaderName());

        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources") || path.startsWith("/actuator")) {
            return new ApiKeyAuthentication("swagger-access", null);
        }

     
        if (requestApiKey == null || !requestApiKey.equals(getApiKey())) {
            log.warn("Invalid API key attempt from IP: {}", requestApiKey);
            throw new InvalidApiKeyException("Invalid API key attem from IP: " + request.getRemoteAddr()
                    + "Eingetragene API_key " + requestApiKey);
        }

        return new ApiKeyAuthentication(getApiKey(),
                null);
    }

    /**
     * Register a new user
     * 
     * @param input User registration data
     * @return Registered user information
     * @throws RoleNotFoundException     if the default user role is not found
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    @Transactional
    public RegisterUserDto signup(@Valid RegisterUserDto input) throws RoleNotFoundException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        Optional<Role> optionalRole = roleRepository.findByName(Role_Enum.USER);
        if (optionalRole.isEmpty()) {
            throw new RoleNotFoundException("Role User not found !");
        }
        User user = modelMapper.map(input, User.class);
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setPlainPassword(input.getPassword());
        if (!input.getRfidNummer().isEmpty()) {
            user.setRfidnummer(input.getRfidNummer());
        }

        user.getRoles().add(optionalRole.get());
        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        return modelMapper.map(savedUser, RegisterUserDto.class);

    }

    /**
     * Authenticate a user with rfidNumber
     * 
     * @param input RfidLoginCredentials
     * @return Authenticated user
     * @throws AuthenticationException if authenticattion fails
     */
    public User authenticateWithRfid(RfidLoginUserDto input) throws AuthenticationException {
        User inputUser = userRepository.findByRfidnummer(input.getRfidNummer())
                .orElseThrow(() -> new RfidNotFoundException(
                        "Benutzer mit der RFID-Nummer \"" + input.getRfidNummer() + "\" wurde nicht gefunden."));

        inputUser.setPassword(inputUser.getPlainPassword());

        validateLinieAccess(inputUser, input.getMontageLinie());

        Authentication authentication = authenticateUser(inputUser, input.getMontageLinie());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return inputUser;
    }

    public User authenticateAsAdministrator(LoginUserDto input) throws AuthenticationException {
        User inputUser = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Administrator mit dem Benutzernamen \"" + input.getUsername() + "\" wurde nicht gefunden."));

        inputUser.setPassword(input.getPassword());
        inputUser.setUsername(input.getUsername());

        if (userHasRole(inputUser, "ADMIN")) {
            Authentication authentication = authenticateWithManager(inputUser);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return inputUser;
        } else {
            throw new AccessDeniedException(
                    "Zugriff verweigert: Sie verfügen nicht über die erforderlichen Administratorrechte.");
        }
    }

    /**
     * Authenticate a user login
     * 
     * @param input Login credentials
     * @return Authenticated user
     * @throws AuthenticationException if authentication fails
     */
    public User authenticate(LoginUserDto input)
            throws AuthenticationException {
        User inputUser;

        inputUser = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(input.getUsername() + " was not found!"));

        inputUser.setUsername(input.getUsername());
        inputUser.setPassword(input.getPassword());

        // Validiere Zugriffsdauer
        validateLinieAccess(inputUser, input.getMontageBezeichnung());

        // Verify the user has access to the requested line
        log.debug("Authenticating user for line: {}", input.getMontageBezeichnung());

        // Perform authentication based on role and line access

        Authentication authentication = authenticateUser(inputUser, input.getMontageBezeichnung());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return inputUser;
    }

    /**
     * Prüfe ob Zugriff für Linie noch gültig ist
     * 
     */
    private void validateLinieAccess(User user, String montageBezeichnung) throws AccessExpiredException {
        if (montageBezeichnung == null || montageBezeichnung.isBlank()) {
            return;
        }

        Optional<LinieAccessConfig> accessConfig = user.getLinienAccessConfigs().stream().filter(
                config -> config.getLinie().getName().equalsIgnoreCase(montageBezeichnung)).findFirst();
        if (accessConfig.isPresent()) {
            LinieAccessConfig config = accessConfig.get();
            LocalDate endDate = config.getAccessStartDate().plusDays(config.getAccessDurationDays());

            if (LocalDate.now().isAfter(endDate)) {
                log.warn("Access expired for user {} on line {}", user.getUsername(), montageBezeichnung);
                throw new AccessExpiredException("Ihr Zugriff auf die Linie " + montageBezeichnung + " is abgelaufen. "
                        + "Bitte kontaktieren Sie einen Administrator.");
            }
        }
    }

    /**
     * Authenticate a user based on role and line access
     * 
     * @param user               User to authenticate
     * @param montageBezeichnung The requested line
     * @return Authentication object
     * @throws AccessLinieDeniedException
     */

    private Authentication authenticateUser(User user, String montageBezeichnung) throws AccessLinieDeniedException {
        try {
            String linienName;

            if (montageBezeichnung != null) {
                if (!montageBezeichnung.isBlank()) {
                    linienName = montageBezeichnung;
                } else {
                    throw new LinieNotFoundException(
                            "Bitte geben Sie die Montagelinie an, in der Sie arbeiten möchten.");
                }
            } else {
                throw new AccessLinieDeniedException(
                        "Sie haben keine Berechtigung für eine Linie oder sind kein Administrator.");
            }

            if (userHasLinieAccess(user, montageBezeichnung)) {
                return authenticateWithManager(user);
            } else {
                Linie linie = linieRepository.findByName(linienName)
                        .orElseThrow(() -> new LinieNotFoundException(
                                "Die Linie \"" + montageBezeichnung + "\" existiert nicht."));
                throw new AccessLinieDeniedException(
                        "Sie haben keinen Zugriff auf die Linie \"" + linie.getName() + "\".");
            }

        } catch (IllegalArgumentException e) {
            throw new LinieNotFoundException("Die angegebene Linie \"" + montageBezeichnung + "\" ist ungültig.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Anmeldung fehlgeschlagen. Bitte überprüfen Sie Ihre Zugangsdaten.");
        }
    }

    /**
     * Check if user has a specific role
     * 
     * @param user     User to check
     * @param roleName Role name to check
     * @return true if user has the role
     */
    private boolean userHasRole(User user, String roleName) {
        return user.getRoles().stream().map(role -> role.getName().toString()).anyMatch(r -> r.equals(roleName));
    }

    /**
     * Check if user has access to a specific line
     * 
     * @param user               User to check
     * @param montageBezeichnung Line name to check
     * @return true if user has access to the line
     */
    private boolean userHasLinieAccess(User user, String montageBezeichnung) {
        return user.getLinien().stream().map(linie -> linie.getName().toString())
                .anyMatch(l -> l.equalsIgnoreCase(montageBezeichnung));
    }

    /**
     * Authenticate with the authentication manager
     * 
     * @param user User to authenticate
     * @return Authentication object
     */
    private Authentication authenticateWithManager(User user) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Zugangsdaten sind ungültig");
        }

    }

    /**
     * Find a user by username
     * 
     * @param username to search for
     * @return Found user
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

}
