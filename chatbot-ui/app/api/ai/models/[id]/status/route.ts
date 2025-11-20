import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AI_SERVICE_BASE_URL, AUTH_COOKIE_NAME} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${AI_SERVICE_BASE_URL}/ai/models`;

async function resolveToken() {
  const cookieStore = await cookies();
  return cookieStore.get(AUTH_COOKIE_NAME)?.value ?? cookieStore.get("satoken")?.value ?? "";
}

function authHeaders(token?: string) {
  const headers = new Headers();
  if (token) {
    headers.set("Authorization", token.startsWith("Bearer ") ? token : `Bearer ${token}`);
  }
  headers.set("Content-Type", "application/json");
  return headers;
}

export async function PATCH(request: Request, context: { params: { id: string } }) {
  const token = await resolveToken();
  const target = `${BASE}/${encodeURIComponent(context.params.id)}/status`;
  try {
    const payload = await request.text();
    const resp = await apiFetch(target, {
      method: "PATCH",
      headers: authHeaders(token),
      body: payload,
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (error) {
    return NextResponse.json(
      { code: 500, msg: error instanceof Error ? error.message : "更新模型状态失败" },
      { status: 500 }
    );
  }
}
