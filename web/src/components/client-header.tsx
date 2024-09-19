"use client"
import React, {useEffect, useState} from "react";
import {EmptyHeader, SessionHeader} from "@/components/auth/auth-header";
import {useSession} from "next-auth/react";

interface Props {
    title? : string
}

export const ClientHeader = ({title} : Props) => {
    const [headerElement, setHeaderElement] = useState<React.JSX.Element | null>(null);
    const { data: session, status } = useSession();

    useEffect(() => {
        if(session) {
            SessionHeader({title : title, session : session}).then(html => setHeaderElement(html));
        } else {
            setHeaderElement(<EmptyHeader title={title}/>);
        }
    }, [status]);

    return headerElement;
}

