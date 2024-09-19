import {ApikeyInfo, Page, BellaResponse} from "@/types/openapi";


export async function getApikeyInfos(page: number): Promise<Page<ApikeyInfo> | null> {
    try {
        const response = await fetch(`/api/openapi/console/apikey/page?status=active&page=${page}`, {
            method: 'GET',
        });
        const result: BellaResponse<Page<ApikeyInfo>> = await response.json();

        if (result.code === 200) {
            return result.data || null;
        } else {
            console.error('Failed to fetch api:', result.message);
            return null;
        }
    } catch (error) {
        console.error('Error fetching api:', error);
        throw error;
    }
}

export async function deleteApikey(code : string) : Promise<boolean> {
    const response = await fetch(`/api/openapi/console/apikey/inactive`, {
        method: 'POST',
        body: JSON.stringify({
            code: code
        })
    });
    const result: BellaResponse<boolean> = await response.json();
    return result.data ? result.data : false;
}

export async function resetApikey(code : string) : Promise<string | null> {
    const response = await fetch(`/api/openapi/console/apikey/reset`, {
        method: 'POST',
        body: JSON.stringify({
            code: code
        })
    });
    const result: BellaResponse<string> = await response.json();
    return result.data || null;
}


export async function updateCertify(code : string, certifyCode : string) : Promise<boolean> {
    const response = await fetch('/api/openapi/console/apikey/certify', {
        method: 'POST',
        body: JSON.stringify({
            code: code,
            certifyCode,
        }),
    });
    const result: BellaResponse<boolean> = await response.json();
    return result.data ? result.data : false;
}

export async function updateQuota(code : string, monthQuota : number) : Promise<boolean> {
    const response = await fetch('/api/openapi/console/apikey/quota/update', {
        method: 'POST',
        body: JSON.stringify({
            code: code,
            monthQuota,
        }),
    });
    const result: BellaResponse<boolean> = await response.json();
    return result.data ? result.data : false;
}

