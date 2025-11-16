import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_SERVICE_BASE_URL} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${PLATFORM_SERVICE_BASE_URL}/files`;

/**
 * 代理到平台文件上传接口（multipart/form-data 透传）
 * 说明：用平台存储替代 vercel/blob，保持统一鉴权与日志
 */
export async function POST(request: Request) {
  try {
    const cookieStore = await cookies();
    const token =
      cookieStore.get(AUTH_COOKIE_NAME)?.value ||
      cookieStore.get("satoken")?.value ||
      "";

    const form = await request.formData();
    const headers = new Headers();
    if (token) headers.set("Authorization", `Bearer ${token}`);
    // 不设置 Content-Type，交由 fetch 自动生成 boundary

    const resp = await apiFetch(`${BASE}/upload`, {
      method: "POST",
      headers,
      body: form,
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "上传失败" },
      { status: 500 }
    );
  }
}
