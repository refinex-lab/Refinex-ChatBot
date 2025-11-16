import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_SERVICE_BASE_URL} from "@/lib/env";

const BASE = `${PLATFORM_SERVICE_BASE_URL}/files`;

export async function GET(_request: Request, { params }: { params: { id: string } }) {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";
  try {
    const headers = new Headers();
    if (token) headers.set("Authorization", `Bearer ${token}`);
    // 直接请求平台并以流的形式透传
    const resp = await fetch(`${BASE}/${encodeURIComponent(params.id)}/download`, {
      method: "GET",
      headers,
    });
    const resHeaders = new Headers();
    const copy = ["content-type", "content-disposition", "content-length", "cache-control", "etag"];
    copy.forEach((k) => {
      const v = resp.headers.get(k);
      if (v) resHeaders.set(k, v);
    });
    return new NextResponse(resp.body, { status: resp.status, headers: resHeaders });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "下载失败" },
      { status: 500 }
    );
  }
}

