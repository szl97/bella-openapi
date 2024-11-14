import { DropdownMenu, DropdownMenuContent, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import React from "react"

export interface UserInfo {
    userId: number
    userName: string
    image?: string
}

interface UserNavProps {
    user: UserInfo
}

export function UserNav({ user }: UserNavProps) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Avatar className="h-8 w-8 cursor-pointer">
                    <AvatarImage src={user.image} alt={user.userName} />
                    <AvatarFallback>{user.userName.charAt(0)}</AvatarFallback>
                </Avatar>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56" align="end">
                <div className="flex items-center justify-center p-2">
                    <span className="text-sm font-medium">{user.userName}</span>
                </div>
            </DropdownMenuContent>
        </DropdownMenu>
    )
}
