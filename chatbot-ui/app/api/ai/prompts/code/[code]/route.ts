import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AI_SERVICE_BASE_URL, AUTH_COOKIE_NAME} from "@/lib/env";
import {apiFetch} from "@/lib/http";

// 提示词 API 路由（根据代码查询）
const BASE = `${AI_SERVICE_BASE_URL}/ai/prompts/code`;

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
 * @returns 认证头
 */
function authHeaders(token?: string) {
  const headers = new Headers();
  if (token) {
    headers.set("Authorization", token.startsWith("Bearer ") ? token : `Bearer ${token}`);
  }
  return headers;
}

/**
 * 查询提示词
 *
 * @param _ 请求
 * @param context 上下文
 * @returns 提示词
 */
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
      { code: 500, msg: error instanceof Error ? error.message : "查询提示词失败" },
      { status: 500 }
    );
  }
}
