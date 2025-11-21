import {NextResponse} from "next/server";
import {cookies} from "next/headers";
import {AI_SERVICE_BASE_URL, AUTH_COOKIE_NAME} from "@/lib/env";
import {apiFetch} from "@/lib/http";

async function resolveToken() {
  const cookieStore = await cookies();
  return (
    cookieStore.get(AUTH_COOKIE_NAME)?.value ??
    cookieStore.get("satoken")?.value ??
    ""
  );
}

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

function appendQuery(base: string, requestUrl: string) {
  const url = new URL(requestUrl, "http://localhost");
  const qs = url.searchParams.toString();
  return qs ? `${base}?${qs}` : base;
}

async function forward({
  target,
  method,
  token,
  body,
  asJson,
}: {
  target: string;
  method: string;
  token: string;
  body?: string;
  asJson?: boolean;
}) {
  const resp = await apiFetch(target, {
    method,
    headers: authHeaders(token, Boolean(asJson)),
    body,
    cache: method === "GET" ? "no-store" : undefined,
  });
  const data = await resp.json().catch(() => ({}));
  return NextResponse.json(data, { status: resp.status });
}

function handleError(resource: string, action: string, error: unknown) {
  return NextResponse.json(
    {
      code: 500,
      msg: error instanceof Error ? error.message : `${action}${resource}失败`,
    },
    { status: 500 }
  );
}

export function createCollectionHandlers(path: string, resourceName: string) {
  const base = `${AI_SERVICE_BASE_URL}${path}`;
  return {
    GET: async (request: Request) => {
      const token = await resolveToken();
      try {
        const target = appendQuery(base, request.url);
        return await forward({ target, method: "GET", token });
      } catch (error) {
        return handleError(resourceName, "获取", error);
      }
    },
    POST: async (request: Request) => {
      const token = await resolveToken();
      try {
        const payload = await request.text();
        return await forward({ target: base, method: "POST", token, body: payload, asJson: true });
      } catch (error) {
        return handleError(resourceName, "创建", error);
      }
    },
  };
}

export function createEntityHandlers(path: string, resourceName: string) {
  const base = `${AI_SERVICE_BASE_URL}${path}`;
  return {
    GET: async (_: Request, context: { params: { id: string } }) => {
      const token = await resolveToken();
      const target = `${base}/${encodeURIComponent(context.params.id)}`;
      try {
        return await forward({ target, method: "GET", token });
      } catch (error) {
        return handleError(resourceName, "查询", error);
      }
    },
    PUT: async (request: Request, context: { params: { id: string } }) => {
      const token = await resolveToken();
      const target = `${base}/${encodeURIComponent(context.params.id)}`;
      try {
        const payload = await request.text();
        return await forward({ target, method: "PUT", token, body: payload, asJson: true });
      } catch (error) {
        return handleError(resourceName, "更新", error);
      }
    },
    DELETE: async (_: Request, context: { params: { id: string } }) => {
      const token = await resolveToken();
      const target = `${base}/${encodeURIComponent(context.params.id)}`;
      try {
        return await forward({ target, method: "DELETE", token });
      } catch (error) {
        return handleError(resourceName, "删除", error);
      }
    },
  };
}

export function createStatusHandlers(path: string, resourceName: string) {
  const base = `${AI_SERVICE_BASE_URL}${path}`;
  return {
    PATCH: async (request: Request, context: { params: { id: string } }) => {
      const token = await resolveToken();
      const target = `${base}/${encodeURIComponent(context.params.id)}/status`;
      try {
        const payload = await request.text();
        return await forward({ target, method: "PATCH", token, body: payload, asJson: true });
      } catch (error) {
        return handleError(resourceName, "更新状态", error);
      }
    },
  };
}
