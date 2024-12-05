import {NextRequest, NextResponse} from 'next/server';
import {workflow_url, workflow_apikey} from "../config";

const WORKFLOW_API_URL = workflow_url;
const API_KEY = workflow_apikey;

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

    const body = {
      model,
      endpoint,
      start,
      end
    };

    const response = await fetch(WORKFLOW_API_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        tenantId: "04633c4f-8638-43a3-a02e-af23c29f821f",
        workflowId: "WKFL-0e8d61e3-c616-44b4-99fc-e14284cc9b4c",
        inputs: {
          code: body.model,
          endpoint: body.endpoint,
          startTime: body.start,
          endTime: body.end
        },
        responseMode: "blocking"
      })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Failed to fetch metrics');
    }

    // 处理返回的数据，将 metrics 字符串转换为对象
    const processedData = data.data.outputs.result.list.map((item: any) => ({
      ...item,
      metrics: JSON.parse(item.metrics)
    }));

    return NextResponse.json(processedData);
  } catch (error) {
    console.error('Error fetching metrics:', error);
    return NextResponse.json(
      { error: 'Failed to fetch metrics' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {

  try {
    const body = await request.json();
    const { startTime, endTime } = body;

    const response = await fetch(WORKFLOW_API_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${API_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        tenantId: "04633c4f-8638-43a3-a02e-af23c29f821f",
        workflowId: "WKFL-0e8d61e3-c616-44b4-99fc-e14284cc9b4c",
        inputs: {
          code: "gpt-4o",
          endpoint: "/v1/chat/completions",
          startTime,
          endTime
        },
        responseMode: "blocking"
      })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Failed to fetch metrics');
    }

    // 处理返回的数据，将 metrics 字符串转换为对象
    const processedData = data.data.outputs.result.list.map((item: any) => ({
      ...item,
      metrics: JSON.parse(item.metrics)
    }));

    return NextResponse.json(processedData);
  } catch (error) {
    console.error('Error fetching metrics:', error);
    return NextResponse.json(
      { error: 'Failed to fetch metrics' },
      { status: 500 }
    );
  }
}
