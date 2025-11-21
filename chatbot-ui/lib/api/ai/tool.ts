/**
 * AI 工具 API（映射 refinex-ai AiToolController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";
import type {PageResult} from "@/lib/api/ai/provider";

export type AiTool = {
  id: number;
  toolCode: string;
  toolName: string;
  toolType: string;
  implBean?: string | null;
  endpoint?: string | null;
  timeoutMs?: number | null;
  inputSchema?: Record<string, unknown> | null;
  outputSchema?: Record<string, unknown> | null;
  mcpServerId?: number | null;
  status: number;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
};

export type ToolQuery = {
  toolType?: string;
  mcpServerId?: number;
  status?: number;
  keyword?: string;
  current?: number;
  size?: number;
};

export type ToolMutationPayload = {
  toolCode: string;
  toolName: string;
  toolType: string;
  implBean?: string;
  endpoint?: string;
  timeoutMs?: number;
  inputSchema?: Record<string, unknown>;
  outputSchema?: Record<string, unknown>;
  mcpServerId?: number;
  status?: number;
  remark?: string;
};

function buildQuery(params?: ToolQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.toolType) search.set("toolType", params.toolType);
  if (params.mcpServerId) search.set("mcpServerId", String(params.mcpServerId));
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function queryTools(params?: ToolQuery): Promise<PageResult<AiTool>> {
  const qs = buildQuery(params).toString();
  const resp = await aiApi(`/tools${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiTool> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取工具列表失败");
  }
  return json.data;
}

export async function getTool(id: number): Promise<AiTool | null> {
  const resp = await aiApi(`/tools/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiTool | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询工具失败");
}

export async function createTool(payload: ToolMutationPayload): Promise<void> {
  const resp = await aiApi(`/tools`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建工具失败");
  }
}

export async function updateTool(id: number, payload: ToolMutationPayload): Promise<void> {
  const resp = await aiApi(`/tools/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新工具失败");
  }
}

export async function deleteTool(id: number): Promise<void> {
  const resp = await aiApi(`/tools/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除工具失败");
  }
}

export async function updateToolStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/tools/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新工具状态失败");
  }
}
