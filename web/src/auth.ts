import NextAuth, {Session, User} from "next-auth"
import Credentials from "next-auth/providers/credentials"
import {JWT} from "next-auth/jwt";
import {WhoAmIHandler, BellaWhoAmIHandler} from "@/app/api/auth/whoami"

const jwt = async ({ token, user }: { token: JWT; user?: User }) => {
    if(user) {
        token.user = user;
        return token;
    }
    return token;
};

const session = ({ session, token }: { session: Session; token: JWT })
    : Promise<Session> => {
    session.name = token.user?.name || "";
    session.thirdPartSource = token.user?.thirdPartSource || false;
    session.token = token.token;
    return Promise.resolve(session);
};


// @ts-ignore
export const { handlers, signIn, signOut, auth } = NextAuth({
    providers: [
        Credentials({
            id: 'apikey',
            name: 'apikey',
            // You can specify which fields should be submitted, by adding keys to the `credentials` object.
            // e.g. domain, username, password, 2FA token, etc.
            credentials: {
                apikey: {},
            },
            authorize: async (credentials) => {
                let user = null
                // logic to apikey if the user exists
                user = await WhoAmIHandler(credentials.apikey as string);

                if (!user) {
                    // No user found, so this is their first attempt to login
                    // meaning this is also the place you could do registration
                    throw new Error("User not found.")
                }

                // return user object with their profile data
                return user
            },
        }),
        Credentials({
            id: 'bellaKey',
            name: 'Bella Key',
            // You can specify which fields should be submitted, by adding keys to the `credentials` object.
            // e.g. domain, username, password, 2FA token, etc.
            credentials: {
                bellaKey:{},
            },
            authorize: async (credentials) => {
                let user = null

                // logic to apikey if the user exists
                user = await BellaWhoAmIHandler(credentials.bellaKey as string);

                if (!user) {
                    // No user found, so this is their first attempt to login
                    // meaning this is also the place you could do registration
                    throw new Error("User not found.")
                }

                // return user object with their profile data
                return user
            },
        }),
    ],
    session: {
        strategy: "jwt",
    },
    callbacks: {
        jwt,
        session,
    },
    theme: {
        colorScheme: 'auto',
        brandColor: 'black'
    },
})
