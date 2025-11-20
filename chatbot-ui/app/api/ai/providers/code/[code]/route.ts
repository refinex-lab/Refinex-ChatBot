import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AI_SERVICE_BASE_URL, AUTH_COOKIE_NAME} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${AI_SERVICE_BASE_URL}/ai/providers/code`;

async function resolveToken() {
  const cookieStore = await cookies();
  return (
    cookieStore.get(AUTH_COOKIE_NAME)?.value ??
    cookieStore.get("satoken")?.value ??
    ""
  );
}

function authHeaders(token?: string) {
  const headers = new Headers();
  if (token) {
    headers.set("Authorization", token.startsWith("Bearer ") ? token : `Bearer ${token}`);
  }
  return headers;
}

export async function GET(_: Request, context: { params: { code: string } }) {
  const token = await resolveToken();
  const target = `${BASE}/${encodeURIComponent(context.params.code)}`;
  try {
    const resp = await apiFetch(target, {
      method: "GET",
      headers: authHeaders(token),
      cache: "no-store",
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (error) {
    return NextResponse.json(
      { code: 500, msg: error instanceof Error ? error.message : "查询模型供应商失败" },
      { status: 500 }
    );
  }
}
