/**
 * AI Advisor API（映射 refinex-ai AiAdvisorController）
 */
import {aiApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";
import type {PageResult} from "@/lib/api/ai/provider";

export type AiAdvisor = {
  id: number;
  advisorCode: string;
  advisorName: string;
  advisorType: string;
  sort?: number | null;
  status: number;
  remark?: string | null;
  createTime?: string | null;
  updateTime?: string | null;
};

export type AdvisorQuery = {
  advisorType?: string;
  status?: number;
  keyword?: string;
  current?: number;
  size?: number;
};

export type AdvisorMutationPayload = {
  advisorCode: string;
  advisorName: string;
  advisorType: string;
  sort?: number;
  status?: number;
  remark?: string;
};

function buildQuery(params?: AdvisorQuery) {
  const search = new URLSearchParams();
  if (!params) return search;
  if (params.advisorType) search.set("advisorType", params.advisorType);
  if (typeof params.status === "number") search.set("status", String(params.status));
  if (params.keyword) search.set("keyword", params.keyword);
  if (params.current) search.set("current", String(params.current));
  if (params.size) search.set("size", String(params.size));
  return search;
}

export async function queryAdvisors(params?: AdvisorQuery): Promise<PageResult<AiAdvisor>> {
  const qs = buildQuery(params).toString();
  const resp = await aiApi(`/advisors${qs ? `?${qs}` : ""}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<PageResult<AiAdvisor> | null>;
  if (json.code !== 200 || !json.data) {
    throw new Error(json.msg || "获取 Advisor 列表失败");
  }
  return json.data;
}

export async function getAdvisor(id: number): Promise<AiAdvisor | null> {
  const resp = await aiApi(`/advisors/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<AiAdvisor | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询 Advisor 失败");
}

export async function createAdvisor(payload: AdvisorMutationPayload): Promise<void> {
  const resp = await aiApi(`/advisors`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "创建 Advisor 失败");
  }
}

export async function updateAdvisor(id: number, payload: AdvisorMutationPayload): Promise<void> {
  const resp = await aiApi(`/advisors/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 Advisor 失败");
  }
}

export async function deleteAdvisor(id: number): Promise<void> {
  const resp = await aiApi(`/advisors/${id}`, { method: "DELETE" });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "删除 Advisor 失败");
  }
}

export async function updateAdvisorStatus(id: number, status: number): Promise<void> {
  const resp = await aiApi(`/advisors/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) {
    throw new Error(json.msg || "更新 Advisor 状态失败");
  }
}
