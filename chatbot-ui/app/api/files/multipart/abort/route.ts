import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AUTH_COOKIE_NAME, PLATFORM_SERVICE_BASE_URL} from "@/lib/env";
import {apiFetch} from "@/lib/http";

const BASE = `${PLATFORM_SERVICE_BASE_URL}/files/multipart`;

export async function POST(request: Request) {
  const cookieStore = await cookies();
  const token =
    cookieStore.get(AUTH_COOKIE_NAME)?.value ||
    cookieStore.get("satoken")?.value ||
    "";
  try {
    const body = await request.json();
    const headers = new Headers();
    if (token) headers.set("Authorization", `Bearer ${token}`);
    headers.set("Content-Type", "application/json");
    const resp = await apiFetch(`${BASE}/abort`, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    });
    const json = await resp.json().catch(() => ({}));
    return NextResponse.json(json, { status: resp.status });
  } catch (e) {
    return NextResponse.json(
      { code: 500, msg: e instanceof Error ? e.message : "终止分片失败" },
      { status: 500 }
    );
  }
}

