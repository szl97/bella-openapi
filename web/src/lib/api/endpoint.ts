import { EndpointDetails } from '@/lib/types/openapi';
import { openapi } from '@/lib/api/openapi';

export async function getEndpointDetails(endpoint: string, modelName: string, features: string[]): Promise<EndpointDetails> {
    const response = await openapi.get<EndpointDetails>('/v1/meta/endpoint/details', {
        params: { endpoint, modelName, features: features.join(',') },
    });
    return response.data;
}
