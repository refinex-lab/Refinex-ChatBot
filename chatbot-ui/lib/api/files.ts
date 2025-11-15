/**
 * 文件服务 API（对接 refinex-platform FileController）
 *
 * 约定：
 * - 所有接口返回后端统一 ApiResponse<T>，本模块做一次解包与错误抛出
 * - 上传/分片上传使用 multipart/form-data，与后端参数保持一致
 * - 下载返回 Blob，业务方自行处理 URL.createObjectURL 或保存
 */
import {apiFetch} from "@/lib/http";
import {PLATFORM_FILES_BASE_URL,} from "@/lib/env";
import type {ApiResponse} from "@/lib/types/api";

// ---------------- Types ----------------

export type SysFile = {
  id: number;
  storageCode?: string;
  fileKey?: string;
  uri?: string;
  fileName?: string;
  ext?: string;
  mimeType?: string;
  sizeBytes?: number;
  checksumSha256?: string;
  width?: number | null;
  height?: number | null;
  durationMs?: number | null;
  encryptAlgo?: string | null;
  isDbStored?: 0 | 1;
  bizType?: string | null;
  bizId?: string | null;
  title?: string | null;
  sort?: number;
  status?: number;
  createBy?: number | null;
  createTime?: string | null;
  updateBy?: number | null;
  updateTime?: string | null;
  deleted?: number;
  deleteBy?: number | null;
  deleteTime?: string | null;
  remark?: string | null;
};

export type MultipartSession = {
  storageCode: string;
  objectKey: string;
  uploadId: string;
};

export type MultipartPart = {
  partNumber: number;
  eTag: string;
};

// ---------------- File: Simple Upload / Meta / Download / Delete ----------------

export type UploadFileParams = {
  file: Blob; // File 或 Blob
  storageCode?: string;
  bizType?: string;
  bizId?: string;
  title?: string;
  compress?: boolean;
  maxWidth?: number;
  quality?: number; // 0-1
};

/**
 * 上传文件（multipart/form-data）
 */
export async function uploadFile(params: UploadFileParams): Promise<SysFile> {
  const form = new FormData();
  form.append("file", params.file);
  if (params.storageCode) form.append("storageCode", params.storageCode);
  if (params.bizType) form.append("bizType", params.bizType);
  if (params.bizId) form.append("bizId", params.bizId);
  if (params.title) form.append("title", params.title);
  if (typeof params.compress === "boolean") form.append("compress", String(params.compress));
  if (typeof params.maxWidth === "number") form.append("maxWidth", String(params.maxWidth));
  if (typeof params.quality === "number") form.append("quality", String(params.quality));

  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/upload`, {
    method: "POST",
    body: form,
  });
  if (!resp.ok) throw new Error("文件上传失败");
  const json = (await resp.json()) as ApiResponse<SysFile | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "文件上传失败");
  return json.data;
}

/**
 * 获取文件元信息
 */
export async function getFileMeta(id: number): Promise<SysFile> {
  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/${id}`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<SysFile | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "获取文件信息失败");
  return json.data;
}

/**
 * 下载文件为 Blob
 */
export async function downloadFile(id: number): Promise<Blob> {
  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/${id}/download`, { method: "GET" });
  if (!resp.ok) throw new Error("下载文件失败");
  return await resp.blob();
}

/**
 * 删除文件
 */
export async function deleteFile(id: number): Promise<void> {
  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/${id}`, { method: "DELETE" });
  if (!resp.ok) {
    let msg = "删除文件失败";
    try {
      const json = (await resp.json()) as ApiResponse<unknown>;
      msg = json.msg || msg;
    } catch {}
    throw new Error(msg);
  }
}

// ---------------- File: Multipart Upload ----------------

export type InitiateMultipartParams = {
  storageCode?: string;
  fileName: string;
  contentType?: string;
};

/**
 * 初始化分片上传
 */
export async function initiateMultipart(params: InitiateMultipartParams): Promise<MultipartSession> {
  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/multipart/initiate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(params),
  });
  const json = (await resp.json()) as ApiResponse<MultipartSession | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "初始化分片上传失败");
  return json.data;
}

export type UploadPartParams = {
  storageCode: string;
  objectKey: string;
  uploadId: string;
  partNumber: number;
  part: Blob; // 分片数据
};

/**
 * 上传分片
 */
export async function uploadPart(params: UploadPartParams): Promise<MultipartPart> {
  const form = new FormData();
  form.append("storageCode", params.storageCode);
  form.append("objectKey", params.objectKey);
  form.append("uploadId", params.uploadId);
  form.append("partNumber", String(params.partNumber));
  form.append("file", params.part);

  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/multipart/upload-part`, {
    method: "POST",
    body: form,
  });
  const json = (await resp.json()) as ApiResponse<MultipartPart | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "上传分片失败");
  return json.data;
}

export type CompleteMultipartParams = {
  storageCode: string;
  objectKey: string;
  uploadId: string;
  etags: string[];
  fileName: string;
  mimeType?: string;
  bizType?: string;
  bizId?: string;
  title?: string;
};

/**
 * 完成分片上传
 */
export async function completeMultipart(params: CompleteMultipartParams): Promise<SysFile> {
  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/multipart/complete`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(params),
  });
  const json = (await resp.json()) as ApiResponse<SysFile | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "完成分片上传失败");
  return json.data;
}

export type AbortMultipartParams = {
  storageCode: string;
  objectKey: string;
  uploadId: string;
};

/**
 * 终止分片上传
 */
export async function abortMultipart(params: AbortMultipartParams): Promise<void> {
  const resp = await apiFetch(`${PLATFORM_FILES_BASE_URL}/multipart/abort`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(params),
  });
  if (!resp.ok) {
    let msg = "终止分片上传失败";
    try {
      const json = (await resp.json()) as ApiResponse<unknown>;
      msg = json.msg || msg;
    } catch {}
    throw new Error(msg);
  }
}

