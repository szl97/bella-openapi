import {UserInfo} from "@/lib/context/user-context";
import {BellaResponse} from "@/lib/types/openapi";
import {openapi} from '@/lib/api/openapi';

export async function getUserInfo() : Promise<UserInfo> {
    try {
        const response = await openapi.get<UserInfo>('/console/userInfo');
        return response.data || {userId: 0, userName: ""};
    } catch (err) {
        return {userId: 0, userName: ""};
    }
}
