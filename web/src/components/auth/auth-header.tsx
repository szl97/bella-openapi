import {UserNav} from "@/components/auth/user-nav";
import React from "react";
import {Session, User} from "next-auth";
import {auth} from "@/auth";

interface Props {
    title? : string,
    session? : Session | null,
}

export async function SessionHeader({ title, session }: Props) {
    let thirdPartSource;
    let user : User | null;
    if(session) {
        thirdPartSource = session.thirdPartSource || false;
        user = session?.user || null;
    } else {
        const authSession = await auth();
        thirdPartSource = authSession?.thirdPartSource || false;
        user = authSession?.user || null;
    }
    return (<div>
        <header className="flex justify-between items-center p-4 bg-gray-800 text-white">
            <div className="text-lg font-bold">{title || "Bella Openapi"}</div>
            {user && <UserNav user={user} thirdPartSource={thirdPartSource}/>}
        </header>
    </div>)
}

export function EmptyHeader({ title }: Props) {
    return (<div>
        <header className="flex justify-between items-center p-4 bg-gray-800 text-white">
            <div className="text-lg font-bold">{title || "Bella Openapi"}</div>
        </header>
    </div>)
}
