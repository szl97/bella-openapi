import {NextResponse} from 'next/server';
import {workflow_apikey, workflow_url} from "@/app/api/config";
import {callWorkflow} from '@/lib/api/workflow';
export const dynamic = 'force-dynamic';
export const revalidate = 0;

const WORKFLOW_API_URL = workflow_url;
const API_KEY = workflow_apikey;
const TENANT_ID = "04633c4f-8638-43a3-a02e-af23c29f821f";
const WORKFLOW_ID = "WKFL-58db0d85-401e-44f2-b82d-d9fa5b65f7df";

export async function GET() {
    if (!API_KEY) {
        console.error('WORKFLOW_API_KEY is not set in environment variables');
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
    try {
        const inputs = {};
        const workflowResponse = await callWorkflow(
            WORKFLOW_API_URL,
            API_KEY,
            TENANT_ID,
            WORKFLOW_ID,
            inputs
        );
        return NextResponse.json(workflowResponse.data.outputs.result || []);
    } catch (error) {
        console.error('Error fetching service:', error);
        return NextResponse.json(
            { error: error instanceof Error ? error.message : 'Unknown error' },
            { status: 500 }
        );
    }
}
