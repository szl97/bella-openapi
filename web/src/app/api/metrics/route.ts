import {NextRequest, NextResponse} from 'next/server';
import {workflow_apikey, workflow_url} from "@/app/api/config";
import {callWorkflow} from '@/lib/api/workflow';

const WORKFLOW_API_URL = workflow_url;
const API_KEY = workflow_apikey;
const TENANT_ID = "04633c4f-8638-43a3-a02e-af23c29f821f";
const WORKFLOW_ID = "WKFL-58db0d85-401e-44f2-b82d-d9fa5b65f7df";

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
    const model = searchParams.get('model');
    const endpoint = searchParams.get('endpoint');
    const start = searchParams.get('start');
    const end = searchParams.get('end');

    if (!model || !endpoint || !start || !end) {
      return NextResponse.json(
        { error: 'Missing required parameters' },
        { status: 400 }
      );
    }

    const inputs = {
      code: model,
      endpoint: endpoint,
      startTime: start,
      endTime: end
    };

    const workflowResponse = await callWorkflow(
      WORKFLOW_API_URL,
      API_KEY,
      TENANT_ID,
      WORKFLOW_ID,
      inputs
    );

    // 处理返回的数据，将 metrics 字符串转换为对象
    const processedData = workflowResponse.data.outputs.result.list.map((item: any) => ({
      ...item,
      metrics: JSON.parse(item.metrics)
    }));

    return NextResponse.json(processedData);
  } catch (error) {
    console.error('Error fetching metrics:', error);
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Unknown error' },
      { status: 500 }
    );
  }
}
