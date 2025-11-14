import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_AUTH_BASE_URL} from "@/lib/env";
import {apiFetch} from "@/lib/http";

/**
 * 统一登出接口（服务端代理后端，附带 Authorization 头，并清理前端 HttpOnly Cookie）
 */
export async function POST() {
  const cookieStore = await cookies();
  // 读取后端登录 Cookie（与后端 sa-token.token-name 对齐）
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";

  // 组装请求头：后端只读 Header，不读 Cookie
  const headers = new Headers();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let status = 200;
  let body: any = { code: 200, msg: "OK" };
  try {
    const resp = await apiFetch(`${PLATFORM_AUTH_BASE_URL}/logout`, {
      method: "POST",
      headers,
      cache: "no-store",
    });
    status = resp.status;
    try {
      body = await resp.json();
    } catch {
      // ignore parse error
    }
  } catch (e) {
    status = 500;
    body = { code: 500, msg: e instanceof Error ? e.message : "登出失败" };
  }

  // 构造响应并清理本域 HttpOnly Cookie
  const res = NextResponse.json(body, { status });
  res.cookies.delete(AUTH_COOKIE_NAME);
  res.cookies.delete("satoken");
  res.cookies.delete("RX_UID");
  res.cookies.delete("RX_EMAIL");
  return res;
}

