/**
 * AI 模型供应商 API（映射 refinex-ai AiProviderController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";

export type PageResult<T> = {
  records: T[];
  current: number;
  size: number;
  total: number;
  pages: number;
  hasNext: boolean;
  hasPrevious: boolean;
};

export type AiProvider = {
  id: number;
  providerCode: string;
  providerName: string;
  providerType: string;
  baseUrl?: string | null;
  apiKeyCipher?: string | null;
  apiKeyIndex?: string | null;
  rateLimitQpm?: number | null;
  status: number;
  remark?: string | null;
  createBy?: number | null;
  createTime?: string | null;
  updateBy?: number | null;
  updateTime?: string | null;
  deleteBy?: number | null;
  deleteTime?: string | null;
};

export type ProviderQuery = {
  keyword?: string;
  providerType?: string;
  status?: number;
  current?: number;
  size?: number;
};

export type ProviderMutationPayload = {
  providerCode: string;
  providerName: string;
  providerType?: string;
  baseUrl?: string;
  apiKeyCipher?: string;
  apiKeyIndex?: string;
  rateLimitQpm?: number;
  status?: number;
  remark?: string;
};

function buildQuery(params?: ProviderQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.providerType) search.set("providerType", params.providerType);
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function queryProviders(params?: ProviderQuery): Promise<PageResult<AiProvider>> {
  const search = buildQuery(params);
  const qs = search.toString();
  const resp = await aiApi(`/providers${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiProvider> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取模型供应商失败");
  }
  return json.data;
}

export async function getProvider(id: number): Promise<AiProvider | null> {
  const resp = await aiApi(`/providers/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiProvider | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询模型供应商失败");
}

export async function getProviderByCode(code: string): Promise<AiProvider | null> {
  const resp = await aiApi(`/providers/code/${encodeURIComponent(code)}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiProvider | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询模型供应商失败");
}

export async function createProvider(payload: ProviderMutationPayload): Promise<void> {
  const resp = await aiApi(`/providers`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建模型供应商失败");
  }
}

export async function updateProvider(id: number, payload: ProviderMutationPayload): Promise<void> {
  const resp = await aiApi(`/providers/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新模型供应商失败");
  }
}

export async function deleteProvider(id: number): Promise<void> {
  const resp = await aiApi(`/providers/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除模型供应商失败");
  }
}

export async function updateProviderStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/providers/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新模型供应商状态失败");
  }
}
