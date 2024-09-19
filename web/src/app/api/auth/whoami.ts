import { User } from 'next-auth';
import {ApikeyInfo} from "@/types/openapi";
import {BellaResponse} from "@/types/openapi";

export const WhoAmIHandler = async (apikey: string): Promise<User> => {
    const response = await fetch('http://localhost:8081/v1/apikey/whoami', {
        method: 'GET',
        headers: {'Authorization' : `Bearer ${apikey}`}
    });
    const res : BellaResponse<ApikeyInfo> = await response.json();
    if (res.code === 200) {
        const data = res.data;
        const user: User = { apikey: apikey, name: data?.ownerName, userId : data?.userId, thirdPartSource: false, role: data?.rolePath };
        return Promise.resolve(user);
    } else {
        console.error('Failed to fetch api:', res.message);
        throw new Error(res.message);
    }
};

export const BellaWhoAmIHandler = (apikey: string): Promise<User> => {
    const user: User = { apikey: apikey, name: "szl", thirdPartSource: true, userId : 1131313 };
    return Promise.resolve(user);
};
