/**
 * AI Schema API（映射 refinex-ai AiSchemaController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";
import type {PageResult} from "@/lib/api/ai/provider";

export type AiSchema = {
  id: number;
  schemaCode: string;
  schemaName: string;
  schemaType: string;
  schemaJson?: Record<string, unknown> | null;
  version?: number | null;
  strictMode?: number | null;
  status: number;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
};

export type SchemaQuery = {
  schemaType?: string;
  status?: number;
  keyword?: string;
  current?: number;
  size?: number;
};

export type SchemaMutationPayload = {
  schemaCode: string;
  schemaName: string;
  schemaType: string;
  schemaJson?: Record<string, unknown>;
  version?: number;
  strictMode?: number;
  status?: number;
  remark?: string;
};

function buildQuery(params?: SchemaQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.schemaType) search.set("schemaType", params.schemaType);
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function querySchemas(params?: SchemaQuery): Promise<PageResult<AiSchema>> {
  const qs = buildQuery(params).toString();
  const resp = await aiApi(`/schemas${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiSchema> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取 Schema 列表失败");
  }
  return json.data;
}

export async function getSchema(id: number): Promise<AiSchema | null> {
  const resp = await aiApi(`/schemas/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiSchema | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询 Schema 失败");
}

export async function createSchema(payload: SchemaMutationPayload): Promise<void> {
  const resp = await aiApi(`/schemas`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建 Schema 失败");
  }
}

export async function updateSchema(id: number, payload: SchemaMutationPayload): Promise<void> {
  const resp = await aiApi(`/schemas/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 Schema 失败");
  }
}

export async function deleteSchema(id: number): Promise<void> {
  const resp = await aiApi(`/schemas/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除 Schema 失败");
  }
}

export async function updateSchemaStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/schemas/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 Schema 状态失败");
  }
}
