import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AI_SERVICE_BASE_URL, AUTH_COOKIE_NAME} from "@/lib/env";
import {apiFetch} from "@/lib/http";

// 提示词 API 路由
const BASE = `${AI_SERVICE_BASE_URL}/ai/prompts`;

/**
 * 解析认证令牌
 *
 * @returns 认证令牌
 */
async function resolveToken() {
  const cookieStore = await cookies();
  return (
    cookieStore.get(AUTH_COOKIE_NAME)?.value ??
    cookieStore.get("satoken")?.value ??
    ""
  );
}

/**
 * 构建认证头
 *
 * @param token 认证令牌
 * @param asJson 是否作为 JSON 发送
 * @returns 认证头
 */
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

/**
 * 构建查询 URL
 *
 * @param url 请求 URL
 * @returns 查询 URL
 */
function withQuery(url: string) {
  const u = new URL(url);
  const qs = u.searchParams.toString();
  return qs ? `${BASE}?${qs}` : BASE;
}

/**
 * 查询提示词
 *
 * @param request 请求
 * @returns 提示词
 */
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
      { code: 500, msg: error instanceof Error ? error.message : "获取提示词失败" },
      { status: 500 }
    );
  }
}

/**
 * 创建提示词
 *
 * @param request 请求
 * @returns 提示词
 */
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
      { code: 500, msg: error instanceof Error ? error.message : "创建提示词失败" },
      { status: 500 }
    );
  }
}
