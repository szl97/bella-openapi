import {User} from "next-auth";
import {RolePath} from "@/types/openapi";

declare module "next-auth" {
    /**
     * Returned by `useSession`, `getSession` and received as
     * a prop on the `SessionProvider` React Context
     */
    interface Session {
        accessTokenExpires?: string;
        token?: string;
        error?: string;
        name?: string;
        userId?: number;
        thirdPartSource?: boolean;
    }

    interface User {
        name?: string | null;
        apikey?: string;
        userId?: number;
        thirdPartSource?: boolean;
        role?: RolePath;
    }
}

declare module "next-auth/jwt" {
    /** Returned by the `jwt` callback and `getToken`, when using JWT sessions */
    interface JWT {
        user?: User;
        token?: string;
        exp?: number;
        iat?: number;
        jti?: string;
    }
}
