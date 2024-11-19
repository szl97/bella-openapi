import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import Link from 'next/link';
import {hasPermission} from "@/lib/api/userInfo";
import React from "react"
import {UserInfo} from "@/lib/types/openapi";


interface UserNavProps {
    user: UserInfo
}

export function UserNav({ user }: UserNavProps) {
    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Avatar className="h-10 w-10 cursor-pointer border-2 border-gray-500 shadow-lg">
                    <AvatarImage src={user.image} alt={user.userName} />
                    <AvatarFallback className="bg-gray-700 text-white text-lg font-bold">{user.userName.charAt(0)}</AvatarFallback>
                </Avatar>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56" align="end">
                <div className="flex items-center justify-center p-2">
                    <span className="text-sm font-medium">{user.userName}</span>
                </div>
                {hasPermission(user, '/console/model/**') && (
                    <DropdownMenuItem>
                        <Link href="/meta/console" className="w-full text-sm">
                            元数据管理
                        </Link>
                    </DropdownMenuItem>
                )}
            </DropdownMenuContent>
        </DropdownMenu>
    )
}
