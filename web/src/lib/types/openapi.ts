export interface BellaResponse<T> {
    code?: number;
    message?: string;
    timestamp?: number;
    data?: T;
    stacktrace?: string;
}


export interface Page<T> {
    data?: T[];
    has_more: boolean;
    page: number;
    limit: number;
    total: number;
}

export interface ApikeyInfo {
    code: string;
    serviceId: string;
    akSha: string;
    akDisplay: string;
    name: string;
    outEntityCode: string;
    parentCode: string;
    ownerType: string;
    ownerCode: string;
    ownerName: string;
    roleCode: string;
    safetyLevel: number;
    monthQuota: number;
    rolePath?: RolePath;
    status: string;
    remark: string;
    userId: number;
}

export interface RolePath {
    included: string[];
    excluded?: string[];
}


export interface Category {
    id: number;
    categoryCode: string;
    categoryName: string;
    parentCode: string;
    status: string;
    cuid: number;
    cuName: string;
    muid: number;
    muName: string;
    ctime: string;
    mtime: string;
}

export interface Endpoint {
    endpoint: string;
    endpointCode: string;
    endpointName: string;
    maintainerCode: string;
    maintainerName: string;
    status: string;
    cuid: number;
    cuName: string;
    muid: number;
    muName: string;
    ctime: string;
    mtime: string;
}

export interface CategoryTree {
    categoryCode: string;
    categoryName: string;
    endpoints: Endpoint[] | null;
    children: CategoryTree[] | null;
}

export interface MetadataFeature {
    code: string;
    name: string;
}

export interface Model {
    modelName: string;
    documentUrl: string;
    properties: string;
    features: string;
    priceDetails: PriceDetails;
}

export interface EndpointDetails {
    endpoint: string;
    models: Model[];
    features: MetadataFeature[];
    priceDetails: PriceDetails;
}

export interface PriceDetails {
    displayPrice: Record<string, string>;
    unit: string;
}
