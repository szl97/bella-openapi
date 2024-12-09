import { NextResponse } from 'next/server';
import {es_url, es_apikey} from "../config";

const API_KEY = es_apikey;
const API_URL = es_url;

export async function POST(request: Request) {
  try {
    const body = await request.json();

    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'api_key': API_KEY || '',
      },
      body: JSON.stringify({
        stime: body.startTime,
        etime: body.endTime,
        fields: [
          "data_info_msg_akCode",
          "data_info_msg_forwardUrl",
          "data_info_msg_model",
          "data_info_msg_request",
          "data_info_msg_response",
          "data_info_msg_endpoint",
          "data_info_msg_requestId",
          "data_info_msg_bellaTraceId",
          "data_info_msg_requestTime",
          "data_info_msg_user"
        ],
        index: "index-14812-15368",
        queryString: body.query,
        size: body.limit || 100
      })
    });

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: 'Failed to fetch logs' }, { status: 500 });
  }
}
