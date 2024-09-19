export type BellaResponse<T> = {
    code?: number;
    message?: string;
    timestamp?: number;
    data?: T;
    stacktrace?: string;
};


export type Page<T> = {
    data?: T[];
    has_more: boolean;
    page: number;
    limit: number;
    total: number;
};

export type ApikeyInfo = {
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

export type RolePath = {
    included: string[];
    excluded?: string[];
};
