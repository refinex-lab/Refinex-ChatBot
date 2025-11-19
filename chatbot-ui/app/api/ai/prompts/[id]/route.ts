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
 * @param asJson 是否作为 JSON 内容类型
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
 * 查询提示词
 *
 * @param _ 请求
 * @param context 上下文
 * @returns 提示词
 */
export async function GET(_: Request, context: { params: { id: string } }) {
  const token = await resolveToken();
  const target = `${BASE}/${encodeURIComponent(context.params.id)}`;
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

/**
 * 更新提示词
 *
 * @param request 请求
 * @param context 上下文
 * @returns 提示词
 */
export async function PUT(request: Request, context: { params: { id: string } }) {
  const token = await resolveToken();
  const target = `${BASE}/${encodeURIComponent(context.params.id)}`;
  try {
    const payload = await request.text();
    const resp = await apiFetch(target, {
      method: "PUT",
      headers: authHeaders(token, true),
      body: payload,
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (error) {
    return NextResponse.json(
      { code: 500, msg: error instanceof Error ? error.message : "更新提示词失败" },
      { status: 500 }
    );
  }
}

/**
 * 删除提示词
 *
 * @param _ 请求
 * @param context 上下文
 * @returns 提示词
 */
export async function DELETE(_: Request, context: { params: { id: string } }) {
  const token = await resolveToken();
  const target = `${BASE}/${encodeURIComponent(context.params.id)}`;
  try {
    const resp = await apiFetch(target, {
      method: "DELETE",
      headers: authHeaders(token),
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (error) {
    return NextResponse.json(
      { code: 500, msg: error instanceof Error ? error.message : "删除提示词失败" },
      { status: 500 }
    );
  }
}
