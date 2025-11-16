/**
 * 存储配置 API（对接 refinex-platform StorageConfigController）
 * 通过 Next 代理路由调用，避免 HttpOnly Cookie 问题
 */
import {platformApi} from "@/lib/http";
import type {ApiResponse} from "@/lib/types/api";

export type StorageConfig = {
  storageCode: string;
  storageName: string;
  storageType: string;
  endpoint?: string | null;
  region?: string | null;
  bucket?: string | null;
  basePath?: string | null;
  baseUrl?: string | null;
  hasAccessKey: boolean;
  hasSecretKey: boolean;
  sessionPolicy?: string | null;
  isDefault: number;
  extConfig?: string | null;
  status: number;
  remark?: string | null;
};

export type StorageConfigCreateRequest = {
  storageCode: string;
  storageName: string;
  storageType: string;
  endpoint?: string;
  region?: string;
  bucket?: string;
  basePath?: string;
  baseUrl?: string;
  accessKeyPlain?: string; // 明文，后端加密
  secretKeyPlain?: string; // 明文，后端加密
  sessionPolicy?: string; // JSON
  isDefault?: number; // 1/0
  extConfig?: string; // JSON
  status?: number; // 1/0
  remark?: string;
};

export type StorageConfigUpdateRequest = {
  storageName?: string;
  storageType?: string;
  endpoint?: string;
  region?: string;
  bucket?: string;
  basePath?: string;
  baseUrl?: string;
  accessKeyPlain?: string; // 传空串清空
  secretKeyPlain?: string; // 传空串清空
  sessionPolicy?: string;
  isDefault?: number;
  extConfig?: string;
  status?: number;
  remark?: string;
};

/**
 * 列表
 */
export async function listStorageConfigs(): Promise<StorageConfig[]> {
  const resp = await platformApi(`/files/storage/configs`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<StorageConfig[] | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "获取存储配置失败");
  return json.data;
}

/**
 * 按编码查询
 */
export async function getStorageConfig(code: string): Promise<StorageConfig | null> {
  const resp = await platformApi(`/files/storage/configs/${encodeURIComponent(code)}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<StorageConfig | null>;
  if (json.code === 200) return json.data ?? null;
  if (json.code === 404) return null;
  throw new Error(json.msg || "查询存储配置失败");
}

/**
 * 新增
 */
export async function createStorageConfig(req: StorageConfigCreateRequest): Promise<void> {
  const resp = await platformApi(`/files/storage/configs`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
  if (!resp.ok) {
    const json = (await resp.json()) as ApiResponse<unknown>;
    throw new Error(json.msg || "新增存储配置失败");
  }
}

/**
 * 更新
 */
export async function updateStorageConfig(code: string, req: StorageConfigUpdateRequest): Promise<void> {
  const resp = await platformApi(`/files/storage/configs/${encodeURIComponent(code)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
  if (!resp.ok) {
    const json = (await resp.json()) as ApiResponse<unknown>;
    throw new Error(json.msg || "更新存储配置失败");
  }
}

/**
 * 删除
 */
export async function deleteStorageConfig(code: string): Promise<void> {
  const resp = await platformApi(`/files/storage/configs/${encodeURIComponent(code)}`, {
    method: "DELETE",
  });
  if (!resp.ok) {
    const json = (await resp.json()) as ApiResponse<unknown>;
    throw new Error(json.msg || "删除存储配置失败");
  }
}
