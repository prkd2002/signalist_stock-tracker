# Email Setup Instructions for Stocks App

This document provides step-by-step instructions on how to set up email functionality in the Stocks App using Gmail and Nodemailer.

## Issue

If you're seeing the following error:
```
Error: Missing credentials for "PLAIN"
    at ignore-listed frames {
  code: 'EAUTH',
  command: 'API'
}
```

This means that the application is trying to send emails but lacks proper authentication credentials for your Gmail account.

## Solution

### 1. Generate a Gmail App Password

For security reasons, Gmail requires you to use an "App Password" instead of your regular password when using email clients like Nodemailer:

1. Go to your Google Account settings: https://myaccount.google.com/
2. Select "Security" from the left menu
3. Under "Signing in to Google," select "2-Step Verification" (you must have this enabled)
4. At the bottom of the page, select "App passwords"
5. Select "Mail" as the app and "Other" as the device (name it "Stocks App")
6. Click "Generate"
7. Google will display a 16-character password - copy this password

### 2. Update Your .env File

1. Open the `.env` file in your project
2. Find the NODEMAILER section
3. Replace `your_app_password_here` with the 16-character app password you generated:

```
# NODEMAILER
NODEMAILER_EMAIL=your_gmail_address@gmail.com
NODEMAILER_PASSWORD=your_16_character_app_password
```

### 3. Restart Your Application

After updating the .env file, restart your application:

```bash
npm run dev
```

## Troubleshooting

If you're still experiencing issues:

1. **Check Gmail Settings**: Make sure "Less secure app access" is turned on in your Google account settings
2. **Verify Email Address**: Ensure the email address in NODEMAILER_EMAIL matches the one you used to generate the app password
3. **Check for Typos**: Ensure there are no spaces or extra characters in your app password
4. **Gmail Limits**: Be aware that Gmail has sending limits (500 emails per day for regular accounts)

## Additional Information

The application uses Nodemailer to send welcome emails to new users. The email configuration is located in:
- `lib/nodemailer/index.ts` - Main Nodemailer configuration
- `lib/nodemailer/template.ts` - Email templates
- `lib/inngest/functions.ts` - Functions that trigger emails

If you need to modify the email content or behavior, these are the files you should check.