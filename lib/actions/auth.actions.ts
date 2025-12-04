'use server';
import {auth} from "@/lib/better-auth/auth";
import {inngest} from "@/lib/inngest/client";
import {headers} from "next/headers";

export const signUpWithEmail = async ({
                                          email,
                                          password,
                                          fullName,
                                          country,
                                          investmentGoals,
                                          riskTolerance,
                                          preferredIndustry
                                      }: {
    email: string;
    password: string;
    fullName: string;
    country: string;
    investmentGoals: string;
    riskTolerance: string;
    preferredIndustry: string;

}) => {
    try {
        const response = await auth.api.signUpEmail({
            body: {email, password, name: fullName},
        });

        if (response) {
            await inngest.send({
                name: 'app/user.created',
                data: {
                    email: email,
                    name: fullName,
                    country,
                    investmentGoals,
                    riskTolerance,
                    preferredIndustry

                }
            })
        }

        return {
            success: true,
            data: response,

        }


    } catch (err) {
        console.error('Sign up failed', err);
        return {success: false, error: 'Sign up failed'};
    }
}


export const signOut = async () => {
    try {
        await auth.api.signOut({headers: await headers()});

    } catch (err) {
        console.log('Sign out failed', err);
        return {success: false, error: 'Sign out failed'};
    }
}


export const signIn = async ({email, password}: {
    email: string;
    password: string;
}) => {
    try {

        const response = await auth.api.signInEmail({
            body: {
                email, password
            }
        });

        return {success: true, data: response};
    } catch (err) {
        console.error('Sign in Failed', err);
        return {success: false, error: 'Sign in failed'};

    }


}