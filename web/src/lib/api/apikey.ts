import { ApikeyInfo, Page} from "@/lib/types/openapi";
import { openapi } from '@/lib/api/openapi';

export async function getApikeyInfos(page: number, search: string | null): Promise<Page<ApikeyInfo> | null> {
    try {
        const response = await openapi.get<Page<ApikeyInfo>>(`/console/apikey/page`, {
            params: { status: 'active', searchParam: search, page }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching api:', error);
        throw error;
    }
}

export async function applyApikey(ownerCode: string, ownerName: string): Promise<boolean> {
    const response = await openapi.post<boolean>(`/console/apikey/apply`,
        {ownerType:'person', ownerCode: ownerCode, ownerName: ownerName, monthQuota: 50});
    return response.data ?? false;
}

export async function deleteApikey(code: string): Promise<boolean> {
    const response = await openapi.post<boolean>(`/console/apikey/inactive`, { code });
    return response.data ?? false;
}

export async function resetApikey(code: string): Promise<string | null> {
    const response = await openapi.post<string>(`/console/apikey/reset`, { code });
    return response.data || null;
}

export async function updateCertify(code: string, certifyCode: string): Promise<boolean> {
    const response = await openapi.post<boolean>('/console/apikey/certify', { code, certifyCode });
    return response.data ?? false;
}

export async function updateQuota(code: string, monthQuota: number): Promise<boolean> {
    const response = await openapi.post<boolean>('/console/apikey/quota/update', { code, monthQuota });
    return response.data ?? false;
}

export async function rename(code: string, name: string): Promise<boolean> {
    const response = await openapi.post<boolean>('/console/apikey/rename', { code, name });
    return response.data ?? false;
}
