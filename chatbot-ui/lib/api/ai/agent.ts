/**
 * AI Agent API（映射 refinex-ai AiAgentController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";
import type {PageResult} from "@/lib/api/ai/provider";

export type AiAgent = {
  id: number;
  agentCode: string;
  agentName: string;
  description?: string | null;
  modelId: number;
  promptId?: number | null;
  outputSchemaId?: number | null;
  ragKbId?: number | null;
  temperature?: number | null;
  topP?: number | null;
  presencePenalty?: number | null;
  frequencyPenalty?: number | null;
  maxTokens?: number | null;
  stopSequences?: string[] | null;
  toolChoice?: string | null;
  status: number;
  remark?: string | null;
  toolIds?: number[] | null;
  advisorIds?: number[] | null;
  createTime?: string | null;
  updateTime?: string | null;
};

export type AgentQuery = {
  modelId?: number;
  providerId?: number;
  status?: number;
  keyword?: string;
  current?: number;
  size?: number;
};

export type AgentMutationPayload = {
  agentCode: string;
  agentName: string;
  description?: string;
  modelId: number;
  promptId?: number;
  outputSchemaId?: number;
  ragKbId?: number;
  temperature?: number;
  topP?: number;
  presencePenalty?: number;
  frequencyPenalty?: number;
  maxTokens?: number;
  stopSequences?: string[];
  toolChoice?: string;
  status?: number;
  remark?: string;
  toolIds?: number[];
  advisorIds?: number[];
};

function buildQuery(params?: AgentQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.modelId) search.set("modelId", String(params.modelId));
  if (params.providerId) search.set("providerId", String(params.providerId));
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function queryAgents(params?: AgentQuery): Promise<PageResult<AiAgent>> {
  const qs = buildQuery(params).toString();
  const resp = await aiApi(`/agents${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiAgent> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取 Agent 列表失败");
  }
  return json.data;
}

export async function getAgent(id: number): Promise<AiAgent | null> {
  const resp = await aiApi(`/agents/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiAgent | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询 Agent 失败");
}

export async function createAgent(payload: AgentMutationPayload): Promise<void> {
  const resp = await aiApi(`/agents`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建 Agent 失败");
  }
}

export async function updateAgent(id: number, payload: AgentMutationPayload): Promise<void> {
  const resp = await aiApi(`/agents/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 Agent 失败");
  }
}

export async function deleteAgent(id: number): Promise<void> {
  const resp = await aiApi(`/agents/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除 Agent 失败");
  }
}

export async function updateAgentStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/agents/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 Agent 状态失败");
  }
}
