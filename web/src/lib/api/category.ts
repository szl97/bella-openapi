import { Category, CategoryTree } from '@/lib/types/openapi';
import { openapi } from '@/lib/api/openapi';

export async function getTopCategories(): Promise<Category[]> {
    const response = await openapi.get<Category[]>('/v1/meta/category/list', {
        params: { topCategory: true },
    });
    return response.data;
}

export async function getCategoryTree(categoryCode: string): Promise<CategoryTree> {
    const response = await openapi.get<CategoryTree>('/v1/meta/category/tree', {
        params: { includeEndpoint: true, status: 'active', categoryCode },
    });
    return response.data;
}

export async function getAllCategoryTrees(): Promise<CategoryTree[]> {
    const response = await openapi.get<CategoryTree[]>('/v1/meta/category/tree/all');
    return response.data;
}
