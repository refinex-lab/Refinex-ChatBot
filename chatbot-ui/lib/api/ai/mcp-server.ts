/**
 * MCP Server API（映射 refinex-ai AiMcpServerController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";
import type {PageResult} from "@/lib/api/ai/provider";

export type AiMcpServer = {
  id: number;
  serverCode: string;
  serverName: string;
  transportType: string;
  entryCommand?: string | null;
  endpointUrl?: string | null;
  manifestUrl?: string | null;
  authType?: string | null;
  authSecretCipher?: string | null;
  authSecretIndex?: string | null;
  toolsFilter?: string | null;
  status: number;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
};

export type McpQuery = {
  transportType?: string;
  status?: number;
  keyword?: string;
  current?: number;
  size?: number;
};

export type McpMutationPayload = {
  serverCode: string;
  serverName: string;
  transportType: string;
  entryCommand?: string;
  endpointUrl?: string;
  manifestUrl?: string;
  authType?: string;
  authSecretCipher?: string;
  authSecretIndex?: string;
  toolsFilter?: string;
  status?: number;
  remark?: string;
};

function buildQuery(params?: McpQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.transportType) search.set("transportType", params.transportType);
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function queryMcpServers(params?: McpQuery): Promise<PageResult<AiMcpServer>> {
  const qs = buildQuery(params).toString();
  const resp = await aiApi(`/mcp-servers${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiMcpServer> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取 MCP Server 列表失败");
  }
  return json.data;
}

export async function getMcpServer(id: number): Promise<AiMcpServer | null> {
  const resp = await aiApi(`/mcp-servers/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiMcpServer | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询 MCP Server 失败");
}

export async function createMcpServer(payload: McpMutationPayload): Promise<void> {
  const resp = await aiApi(`/mcp-servers`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建 MCP Server 失败");
  }
}

export async function updateMcpServer(id: number, payload: McpMutationPayload): Promise<void> {
  const resp = await aiApi(`/mcp-servers/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 MCP Server 失败");
  }
}

export async function deleteMcpServer(id: number): Promise<void> {
  const resp = await aiApi(`/mcp-servers/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除 MCP Server 失败");
  }
}

export async function updateMcpServerStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/mcp-servers/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 MCP Server 状态失败");
  }
}
