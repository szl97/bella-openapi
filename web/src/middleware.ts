import {NextRequest, NextResponse} from 'next/server';
import {getToken} from 'next-auth/jwt';

export async function middleware(req: NextRequest) {
    const url = req.nextUrl.clone();
    const { pathname } = url;
    if (pathname.startsWith('/api/auth')) {
        return NextResponse.next();
    }

    const token = await getToken({
        req,
        salt : "authjs.session-token",
        secret: process.env.AUTH_SECRET || ""
    });
    if (pathname.startsWith('/api/openapi')) {
        const newUrl = new URL('http://localhost:8081');
        newUrl.pathname = pathname.replace('/api/openapi', '');
        const headers = new Headers();
        if(req.headers.has('host')) {
            headers.set('host', req.headers.get('host') || '');
        }
        if(req.headers.has('origin')) {
            headers.set('origin', req.headers.get('origin') || '');
        }
        if(req.headers.has('referer')) {
            headers.set('referer', req.headers.get('referer') || '');
        }
        if(req.headers.has('user-agent')) {
            headers.set('user-agent', req.headers.get('user-agent') || '');
        }
        newUrl.search = url.search;
        if(token?.user?.apikey) {
            headers.set('Authorization', `Bearer ${token?.user?.apikey}`);
        }
        headers.set("Content-Type", "application/json;charset=UTF-8");
        let body = null;
        if((req.method === 'POST' || req.method === 'PUT') && req.body != null) {
            const clonedReq = req.clone();
            body = await clonedReq.text();
            console.log("body:" + body);
            if (body != null) {
                try {
                    const jsonBody = JSON.parse(body);
                    jsonBody.userId = token?.user?.userId;
                    jsonBody.userName = token?.user?.name;
                    body = JSON.stringify(jsonBody);
                } catch (error) {
                    console.error(error);
                }
            }
        }
        const response = await fetch(newUrl.toString(), {
            method: req.method,
            headers: headers,
            body: body,
            redirect: 'manual',
        });
        return new NextResponse(response.body, {
            status: response.status,
            headers: response.headers,
        });
    }

    if (!token) {
        url.pathname = '/api/auth/signin';
        return NextResponse.redirect(url);
    }
    return NextResponse.next();
}
