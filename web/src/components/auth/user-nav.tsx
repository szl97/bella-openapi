import {User} from "next-auth";
import {
    DropdownMenu,
    DropdownMenuContent, DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";;
import {Avatar, AvatarFallback} from "@/components/ui/avatar";
import Link from "next/link";


interface Props {
    user: User,
    thirdPartSource?: boolean
}

export function UserNav({ user, thirdPartSource }: Props) {
    if(thirdPartSource) {
        return (
            <Avatar className='h-9 w-9 rounded-md'>
                <AvatarFallback>{user.name}</AvatarFallback>
            </Avatar>
        )
    } else {
        return (
            <DropdownMenu>
                <DropdownMenuTrigger asChild>
                    <Avatar className='h-9 w-9'>
                        <AvatarFallback>{user.name}</AvatarFallback>
                    </Avatar>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                    className='w-56'
                    align='end'
                    forceMount>
                    <DropdownMenuItem>
                        <Link
                            href='/api/auth/signout'
                            className='w-full'
                        >
                            Sign Out
                        </Link>
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        )
    }
}
