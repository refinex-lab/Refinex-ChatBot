import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_SERVICE_BASE_URL} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${PLATFORM_SERVICE_BASE_URL}/files`;

function authHeaders(token?: string) {
  const h = new Headers();
  if (token) h.set("Authorization", `Bearer ${token}`);
  return h;
}

export async function GET(_request: Request, { params }: { params: { id: string } }) {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";
  try {
    const resp = await apiFetch(`${BASE}/${encodeURIComponent(params.id)}`, {
      method: "GET",
      headers: authHeaders(token),
      cache: "no-store",
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "获取文件信息失败" },
      { status: 500 }
    );
  }
}

export async function DELETE(_request: Request, { params }: { params: { id: string } }) {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";
  try {
    const resp = await apiFetch(`${BASE}/${encodeURIComponent(params.id)}`, {
      method: "DELETE",
      headers: authHeaders(token),
    });
    const body = await resp.json().catch(() => ({}));
    return NextResponse.json(body, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "删除文件失败" },
      { status: 500 }
    );
  }
}

