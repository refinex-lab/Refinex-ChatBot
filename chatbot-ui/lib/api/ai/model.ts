/**
 * AI 模型 API（映射 refinex-ai AiModelController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";
import type {PageResult} from "@/lib/api/ai/provider";

export type AiModel = {
  id: number;
  providerId: number;
  modelKey: string;
  modelName?: string | null;
  modelType: string;
  apiVariant?: string | null;
  region?: string | null;
  contextWindowTokens?: number | null;
  maxOutputTokens?: number | null;
  priceInputPer1k?: string | number | null;
  priceOutputPer1k?: string | number | null;
  currency: string;
  supportToolCall?: number;
  supportVision?: number;
  supportAudioIn?: number;
  supportAudioOut?: number;
  supportStructuredOut?: number;
  status: number;
  remark?: string | null;
  createBy?: number | null;
  createTime?: string | null;
  updateBy?: number | null;
  updateTime?: string | null;
  deleteBy?: number | null;
  deleteTime?: string | null;
};

export type ModelQuery = {
  providerId?: number;
  modelType?: string;
  apiVariant?: string;
  status?: number;
  keyword?: string;
  current?: number;
  size?: number;
};

export type ModelMutationPayload = {
  providerId: number;
  modelKey: string;
  modelName?: string;
  modelType?: string;
  apiVariant?: string;
  region?: string;
  contextWindowTokens?: number;
  maxOutputTokens?: number;
  priceInputPer1k?: string | number;
  priceOutputPer1k?: string | number;
  currency?: string;
  supportToolCall?: number;
  supportVision?: number;
  supportAudioIn?: number;
  supportAudioOut?: number;
  supportStructuredOut?: number;
  status?: number;
  remark?: string;
};

function buildQuery(params?: ModelQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.providerId) search.set("providerId", String(params.providerId));
  if (params.modelType) search.set("modelType", params.modelType);
  if (params.apiVariant) search.set("apiVariant", params.apiVariant);
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function queryModels(params?: ModelQuery): Promise<PageResult<AiModel>> {
  const search = buildQuery(params);
  const qs = search.toString();
  const resp = await aiApi(`/models${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiModel> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取模型列表失败");
  }
  return json.data;
}

export async function getModel(id: number): Promise<AiModel | null> {
  const resp = await aiApi(`/models/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiModel | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询模型失败");
}

export async function createModel(payload: ModelMutationPayload): Promise<void> {
  const resp = await aiApi(`/models`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建模型失败");
  }
}

export async function updateModel(id: number, payload: ModelMutationPayload): Promise<void> {
  const resp = await aiApi(`/models/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新模型失败");
  }
}

export async function deleteModel(id: number): Promise<void> {
  const resp = await aiApi(`/models/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除模型失败");
  }
}

export async function updateModelStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/models/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新模型状态失败");
  }
}
