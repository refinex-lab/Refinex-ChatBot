import useSWR from "swr";
import {platformApi} from "@/lib/http";
import {ApiResponse} from "@/lib/types/api";

export type UserProfile = {
  userId: number;
  username: string;
  nickname?: string | null;
  avatar?: string | null;
  email?: string | null;
  mobile?: string | null;
  sex?: "MALE" | "FEMALE" | "OTHER" | string | null;
  accountStatus?: number | null;
  status?: number | null;
  lastLoginTime?: string | null;
  lastLoginIp?: string | null;
  roles?: string[];
  permissions?: string[];
};

export type UpdateProfileBody = {
  nickname?: string;
  sex?: "MALE" | "FEMALE" | "OTHER";
  avatar?: string;
  email?: string;
  mobile?: string;
};

export type ChangePasswordBody = {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export async function getProfile(): Promise<UserProfile> {
  const resp = await platformApi(`/user/profile`, { method: "GET", cache: "no-store" });
  const json = (await resp.json()) as ApiResponse<UserProfile | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "获取用户信息失败");
  return json.data;
}

export async function updateProfile(body: UpdateProfileBody): Promise<UserProfile> {
  const resp = await platformApi(`/user/profile`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const json = (await resp.json()) as ApiResponse<UserProfile | null>;
  if (json.code !== 200 || !json.data) throw new Error(json.msg || "更新个人信息失败");
  return json.data;
}

export async function changePassword(body: ChangePasswordBody): Promise<void> {
  const resp = await platformApi(`/user/password`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  const json = (await resp.json()) as ApiResponse<unknown>;
  if (json.code !== 200) throw new Error(json.msg || "修改密码失败");
}

export function useUserProfile() {
  return useSWR<UserProfile>(`/api/user/profile`, async (url: string) => {
    const resp = await platformApi(`/user/profile`, { method: "GET", cache: "no-store" });
    const json = (await resp.json()) as ApiResponse<UserProfile | null>;
    if (json.code !== 200 || !json.data) throw new Error(json.msg || "获取用户信息失败");
    return json.data;
  });
}
