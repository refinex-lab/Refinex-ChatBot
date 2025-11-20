import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AI_SERVICE_BASE_URL, AUTH_COOKIE_NAME} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${AI_SERVICE_BASE_URL}/ai/models`;

async function resolveToken() {
  const cookieStore = await cookies();
  return (
    cookieStore.get(AUTH_COOKIE_NAME)?.value ??
    cookieStore.get("satoken")?.value ??
    ""
  );
}

function authHeaders(token?: string, asJson = false) {
  const headers = new Headers();
  if (token) {
    headers.set("Authorization", token.startsWith("Bearer ") ? token : `Bearer ${token}`);
  }
  if (asJson) {
    headers.set("Content-Type", "application/json");
  }
  return headers;
}

function withQuery(url: string) {
  const u = new URL(url);
  const qs = u.searchParams.toString();
  return qs ? `${BASE}?${qs}` : BASE;
}

export async function GET(request: Request) {
  const token = await resolveToken();
  try {
    const target = withQuery(request.url);
    const resp = await apiFetch(target, {
      method: "GET",
      headers: authHeaders(token),
      cache: "no-store",
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (error) {
    return NextResponse.json(
      { code: 500, msg: error instanceof Error ? error.message : "获取模型失败" },
      { status: 500 }
    );
  }
}

export async function POST(request: Request) {
  const token = await resolveToken();
  try {
    const payload = await request.text();
    const resp = await apiFetch(BASE, {
      method: "POST",
      headers: authHeaders(token, true),
      body: payload,
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (error) {
    return NextResponse.json(
      { code: 500, msg: error instanceof Error ? error.message : "创建模型失败" },
      { status: 500 }
    );
  }
}
