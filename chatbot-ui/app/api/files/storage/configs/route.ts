import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_SERVICE_BASE_URL} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${PLATFORM_SERVICE_BASE_URL}/files/storage/configs`;

function authHeaders(token?: string) {
  const h = new Headers();
  if (token) h.set("Authorization", `Bearer ${token}`);
  return h;
}

export async function GET() {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";
  try {
    const resp = await apiFetch(BASE, {
      method: "GET",
      headers: authHeaders(token),
      cache: "no-store",
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "获取存储配置失败" },
      { status: 500 }
    );
  }
}

export async function POST(request: Request) {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";
  try {
    const body = await request.json();
    const headers = authHeaders(token);
    headers.set("Content-Type", "application/json");
    const resp = await apiFetch(BASE, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    });
    const json = await resp.json().catch(() => ({}));
    return NextResponse.json(json, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "新增存储配置失败" },
      { status: 500 }
    );
  }
}

