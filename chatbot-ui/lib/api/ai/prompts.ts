/**
 * AI 提示词 API （对接 refinex-ai AiPromptController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";

/**
 * 分页查询结果
 */
export type PageResult<T> = {
  records: T[];
  current: number;
  size: number;
  total: number;
  pages: number;
  hasNext: boolean;
  hasPrevious: boolean;
};

/**
 * 提示词
 */
export type AiPrompt = {
  id: number;
  promptCode: string;
  promptName: string;
  category?: string | null;
  description?: string | null;
  templateFormat: string;
  role: string;
  template: string;
  variables?: Record<string, unknown> | null;
  examples?: Array<Record<string, unknown>> | null;
  inputSchema?: Record<string, unknown> | null;
  hashSha256?: string | null;
  status: number;
  remark?: string | null;
  createBy?: number | null;
  createTime?: string | null;
  updateBy?: number | null;
  updateTime?: string | null;
};

/**
 * 提示词查询参数
 */
export type PromptQuery = {
  keyword?: string;
  category?: string;
  templateFormat?: string;
  status?: number;
  current?: number;
  size?: number;
};

/**
 * 提示词变更载荷
 */
export type PromptMutationPayload = {
  promptCode: string;
  promptName: string;
  category?: string;
  description?: string;
  templateFormat: string;
  role: string;
  template: string;
  variables?: Record<string, unknown>;
  examples?: Array<Record<string, unknown>>;
  inputSchema?: Record<string, unknown>;
  status?: number;
  remark?: string;
};

/**
 * 构建提示词查询参数
 *
 * @param params 查询参数
 * @returns 查询参数字符串
 */
function buildQuery(params?: PromptQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.category) search.set("category", params.category);
  if (params.templateFormat) search.set("templateFormat", params.templateFormat);
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

/**
 * 查询提示词列表
 *
 * @param params 查询参数
 * @returns 提示词分页结果
 */
export async function queryPrompts(params?: PromptQuery): Promise<PageResult<AiPrompt>> {
  const search = buildQuery(params);
  const qs = search.toString();
  const resp = await aiApi(`/prompts${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiPrompt> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取提示词失败");
  }
  return json.data;
}

/**
 * 根据ID查询提示词
 *
 * @param id 主键
 * @returns 提示词
 */
export async function getPrompt(id: number): Promise<AiPrompt | null> {
  const resp = await aiApi(`/prompts/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiPrompt | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询提示词失败");
}

/**
 * 根据编码查询提示词
 *
 * @param code 编码
 * @returns 提示词
 */
export async function getPromptByCode(code: string): Promise<AiPrompt | null> {
  const resp = await aiApi(`/prompts/code/${encodeURIComponent(code)}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiPrompt | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询提示词失败");
}

/**
 * 创建提示词
 *
 * @param payload 变更载荷
 */
export async function createPrompt(payload: PromptMutationPayload): Promise<void> {
  const resp = await aiApi(`/prompts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建提示词失败");
  }
}

/**
 * 更新提示词
 *
 * @param id 主键
 * @param payload 变更载荷
 */
export async function updatePrompt(id: number, payload: PromptMutationPayload): Promise<void> {
  const resp = await aiApi(`/prompts/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新提示词失败");
  }
}

/**
 * 删除提示词
 *
 * @param id 主键
 */
export async function deletePrompt(id: number): Promise<void> {
  const resp = await aiApi(`/prompts/${id}`, {
    method: "DELETE",
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除提示词失败");
  }
}
