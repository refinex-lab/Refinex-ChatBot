/**
 * 通用 API 响应
 */
export type ApiResponse<T> = {
  code: number;
  msg: string;
  data: T;
  timestamp?: number;
};
