'use client'

import React from "react"
import { Skeleton } from "@/components/ui/skeleton"
import { UserNav } from "@/components/user/user-nav"
import { useUser } from "@/lib/context/user-context"

interface Props {
    title?: string
}

const EmptyHeader: React.FC<Props> = ({ title }) => (
    <div className="flex items-center justify-between p-4 bg-background border-b">
        <h1 className="text-2xl font-bold">{title}</h1>
    </div>
)

const SessionHeader: React.FC<Props & { userInfo: any }> = ({ title, userInfo }) => (
    <header className="flex justify-between items-center p-4 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-2xl font-bold text-gray-800 dark:text-white">{title || "Bella Openapi"}</h1>
        <UserNav user={userInfo} />
    </header>
)

export const ClientHeader: React.FC<Props> = ({ title }) => {
    const { userInfo, isLoading, error } = useUser();

    if (isLoading) {
        return <Skeleton className="w-full h-16" />
    }

    if (error) {
        return <EmptyHeader title={title} />;
    }

    if (userInfo) {
        return <SessionHeader title={title} userInfo={userInfo} />;
    }

    return <EmptyHeader title={title} />;
}
