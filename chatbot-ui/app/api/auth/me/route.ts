import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_AUTH_BASE_URL} from "@/lib/env";
import {apiFetch} from "@/lib/http";

/**
 * 获取当前登录用户（服务端代理，附带 Authorization 头）
 */
export async function GET() {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";

  const headers = new Headers();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  try {
    const resp = await apiFetch(`${PLATFORM_AUTH_BASE_URL}/me`, {
      method: "GET",
      headers,
      cache: "no-store",
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "获取用户失败" },
      { status: 500 }
    );
  }
}

