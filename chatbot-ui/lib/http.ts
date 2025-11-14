/**
 * 统一的 fetch 包装：为所有后端请求添加 DataSign 等公共头
 */
import {DATA_SIGN_HEADER} from "@/lib/env";

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
 * API Fetch：默认追加 DataSign 头
 */
export async function apiFetch(input: RequestInfo | URL, init?: RequestInit) {
  const headers = mergeHeaders(init);
  return fetch(input, { ...init, headers });
}

