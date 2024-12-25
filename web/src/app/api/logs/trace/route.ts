import {NextRequest, NextResponse} from 'next/server';
import {workflow_apikey, workflow_url} from "@/app/api/config";
import {callWorkflow} from '@/lib/api/workflow';

const WORKFLOW_API_URL = workflow_url;
const API_KEY = workflow_apikey;
const TENANT_ID = "04633c4f-8638-43a3-a02e-af23c29f821f";
const WORKFLOW_ID = "WKFL-197c3c1e-b1f4-47ea-bcb9-7574f5c8edb8";

export async function GET(request: NextRequest) {
    if (!API_KEY) {
        console.error('WORKFLOW_API_KEY is not set in environment variables');
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
    try {
        const searchParams = new URL(request.url).searchParams;
        const serviceId = searchParams.get('serviceId');
        const traceId = searchParams.get('traceId');
        const akCode = searchParams.get('akCode');
        const start = searchParams.get('start');
        const end = searchParams.get('end');

        if (!serviceId || !start || !end || (!traceId && !akCode)) {
            return NextResponse.json(
                { error: 'Missing required parameters' },
                { status: 400 }
            );
        }

        const inputs = {
            serviceId: serviceId,
            traceId: traceId,
            akCode: akCode,
            start: start,
            end: end
        };

        const workflowResponse = await callWorkflow(
            WORKFLOW_API_URL,
            API_KEY,
            TENANT_ID,
            WORKFLOW_ID,
            inputs
        );

        return NextResponse.json(workflowResponse.data.outputs.body || []);
    } catch (error) {
        console.error('Error fetching logs:', error);
        return NextResponse.json(
            { error: error instanceof Error ? error.message : 'Unknown error' },
            { status: 500 }
        );
    }
}
