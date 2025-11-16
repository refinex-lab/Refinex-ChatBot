/**
 * 统一的 fetch 包装：为所有后端请求添加 DataSign 等公共头
 */
import {AUTH_COOKIE_NAME, DATA_SIGN_HEADER} from "@/lib/env";

/**
 * 生成 DataSign，默认使用 UUID（去掉连字符）
 */
function generateDataSign(): string {
  try {
    // 浏览器 + Node 18+ 支持 crypto.randomUUID
    return crypto.randomUUID().replace(/-/g, "");
  } catch {
    // 兜底：时间戳 + 随机数
    return `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 10)}`;
  }
}

/**
 * 合并请求头
 */
function mergeHeaders(init?: RequestInit): Headers {
  const headers = new Headers(init?.headers ?? {});
  if (!headers.has(DATA_SIGN_HEADER)) {
    headers.set(DATA_SIGN_HEADER, generateDataSign());
  }
  return headers;
}

/**
 * 从 Cookie 中解析 token（浏览器环境）
 */
function readCookieTokenClient(): string | undefined {
  try {
    const all = document.cookie || "";
    const map: Record<string, string> = {};
    all.split(";").forEach((pair) => {
      const [k, v] = pair.trim().split("=");
      if (k) map[k] = decodeURIComponent(v || "");
    });
    const names = [AUTH_COOKIE_NAME, "Authorization", "satoken"];
    for (const name of names) {
      if (map[name]) return map[name];
    }
  } catch {}
  return undefined;
}

/**
 * 读取服务端 Cookie（Next.js Route/Server 组件环境）
 */
async function readCookieTokenServer(): Promise<string | undefined> {
  try {
    // 动态导入，避免客户端打包
    const mod = await import("next/headers");
    const cookieStore = await mod.cookies();
    const names = [AUTH_COOKIE_NAME, "Authorization", "satoken"];
    for (const name of names) {
      const v = cookieStore.get(name)?.value;
      if (v) return v;
    }
  } catch {}
  return undefined;
}

/**
 * API Fetch：默认追加 DataSign 头
 */
export async function apiFetch(input: RequestInfo | URL, init?: RequestInit) {
  const headers = mergeHeaders(init);

  // 自动补充 Authorization: Bearer <token>
  if (!headers.has("Authorization")) {
    let token: string | undefined;
    if (typeof window === "undefined") {
      token = await readCookieTokenServer();
    } else {
      token = readCookieTokenClient();
      // 兜底：如果本地存储存在，也可用
      if (!token) {
        try {
          token = localStorage.getItem("RX_TOKEN") || undefined;
        } catch {}
      }
    }
    if (token) {
      const value = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
      headers.set("Authorization", value);
    }
  }

  const credentials = init?.credentials ?? "include";
  return fetch(input, { ...init, headers, credentials });
}

/**
 * 平台统一代理请求：
 * - 自动拼接到 Next 代理路由 `/api${path}`
 * - 继承 apiFetch 能力：DataSign、Authorization、credentials
 *
 * 用法示例：
 *   platformApi('/user/profile', { method: 'GET' })
 *   platformApi('files/upload', { method: 'POST', body: formData })
 */
export function platformApi(path: string, init?: RequestInit) {
  const p = path.startsWith("/") ? path : `/${path}`;
  return apiFetch(`/api${p}`, init);
}
