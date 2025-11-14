/**
 * 认证动作
 */
"use server";

import {z} from "zod";

import {PLATFORM_AUTH_BASE_URL} from "@/lib/env";
import type {ApiResponse} from "@/lib/types/api";
import {apiFetch} from "@/lib/http";
import {cookies} from "next/headers";

/**
 * 认证表单模式
 */
const loginFormSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
  captchaUuid: z.string().optional(),
  captchaCode: z.string().optional(),
  rememberMe: z.boolean().optional(),
});

/**
 * 注册表单模式
 */
const registerFormSchema = z
  .object({
    username: z.string().min(3),
    email: z.string().email(),
    password: z.string().min(8),
    confirmPassword: z.string().min(8),
    nickname: z.string().optional(),
    captchaUuid: z.string().min(1),
    captchaCode: z.string().min(1),
  })
  .refine(data => data.password === data.confirmPassword, {
    message: "两次输入的密码不一致",
    path: ["confirmPassword"],
  });

/**
 * 登录状态：idle(空闲) | in_progress(进行中) | success(成功) | failed(失败) | invalid_data(数据无效)
 */
export type LoginActionState = {
  status: "idle" | "in_progress" | "success" | "failed" | "invalid_data";
  message?: string;
};

/**
 * 登录
 */
export const login = async (
  _: LoginActionState,
  formData: FormData
): Promise<LoginActionState> => {
  // 提取后端错误信息的小工具：优先解析 JSON 的 msg/message 字段，兜底返回简短文本
  async function extractErrorMessage(response: Response): Promise<string> {
    const contentType = response.headers.get("content-type") || "";
    // 优先按 JSON 解析
    if (contentType.includes("application/json")) {
      try {
        const data = (await response.json()) as any;
        if (data && typeof data.msg === "string") return data.msg;
        if (data && typeof data.message === "string") return data.message;
      } catch {
        // ignore
      }
      return `请求失败（${response.status}）`;
    }
    // 非 JSON：尝试读取文本，并尽量避免把整段 JSON/HTML 原样吐给用户
    const text = await response.text().catch(() => "");
    try {
      const data = JSON.parse(text);
      if (data && typeof data.msg === "string") return data.msg;
      if (data && typeof data.message === "string") return data.message;
    } catch {
      // 不是 JSON，返回可读的简短文本（避免超长/HTML）
      const trimmed = (text || "").trim();
      if (trimmed && !trimmed.startsWith("<")) {
        return trimmed.length > 200 ? `${trimmed.slice(0, 200)}...` : trimmed;
      }
    }
    return `请求失败（${response.status}）`;
  }

  try {
    // 验证数据
    const validatedData = loginFormSchema.parse({
      email: formData.get("email"),
      password: formData.get("password"),
      captchaUuid: formData.get("captchaUuid") || undefined,
      captchaCode: formData.get("captchaCode") || undefined,
      rememberMe: formData.get("rememberMe") === "on",
    });

    // 调用后端登录
    const response = await apiFetch(`${PLATFORM_AUTH_BASE_URL}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: validatedData.email,
        password: validatedData.password,
        captchaUuid: validatedData.captchaUuid,
        captchaCode: validatedData.captchaCode,
        rememberMe: Boolean(validatedData.rememberMe),
        deviceType: "PC",
      }),
    });

    if (!response.ok) {
      const message = await extractErrorMessage(response);
      return { status: "failed", message: message || "登录失败" };
    }

    type PlatformLoginUser = {
      userId: number;
      username: string;
      nickname?: string;
      avatar?: string;
      email?: string;
      mobile?: string;
      roles?: string[];
      permissions?: string[];
    };
    type PlatformLoginResponse = {
      tokenName: string;
      tokenValue: string;
      expireIn: number;
      user: PlatformLoginUser;
    };

    const result = (await response.json()) as ApiResponse<PlatformLoginResponse | null>;
    if (!result || result.code !== 200 || !result.data) {
      return { status: "failed", message: result?.msg || "登录失败" };
    }

    // 写入后端 Cookie
    try {
      const tokenName = result.data.tokenName || "satoken";
      const tokenValue = result.data.tokenValue;
      const expires = Math.max(60, result.data.expireIn || 0);
      const cookieStore = await cookies();
      cookieStore.set(tokenName, tokenValue, {
        httpOnly: true,
        sameSite: "lax",
        secure: process.env.NODE_ENV === "production",
        path: "/",
        maxAge: expires,
      });
      // 写入辅助标识，便于前端服务路由使用（仅服务端读取）
      const uid = String(result.data.user?.userId ?? "");
      const email = String(result.data.user?.email ?? "");
      if (uid) {
        cookieStore.set("RX_UID", uid, {
          httpOnly: true,
          sameSite: "lax",
          secure: process.env.NODE_ENV === "production",
          path: "/",
          maxAge: expires,
        });
      }
      if (email) {
        cookieStore.set("RX_EMAIL", email, {
          httpOnly: true,
          sameSite: "lax",
          secure: process.env.NODE_ENV === "production",
          path: "/",
          maxAge: expires,
        });
      }
    } catch {
      // ignore cookie errors
    }

    // 返回成功状态
    return { status: "success" };
  } catch (error) {
    // 验证数据无效
    if (error instanceof z.ZodError) {
      // 返回无效数据状态
      return {
        status: "invalid_data",
        message: error.issues[0]?.message ?? "提交的数据无效",
      };
    }

    // 返回失败状态
    return { status: "failed", message: (error as Error)?.message || "登录失败，请稍后再试" };
  }
};

/**
 * 注册状态
 */
export type RegisterActionState = {
  status:
    | "idle"
    | "in_progress"
    | "success"
    | "failed"
    | "user_exists"
    | "invalid_data";
  message?: string;
};

/**
 * 注册
 */
export const register = async (
  _: RegisterActionState,
  formData: FormData
): Promise<RegisterActionState> => {
  // 提取后端错误信息的小工具：优先解析 JSON 的 msg/message 字段，兜底返回简短文本
  async function extractErrorMessage(response: Response): Promise<string> {
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      try {
        const data = (await response.json()) as any;
        if (data && typeof data.msg === "string") return data.msg;
        if (data && typeof data.message === "string") return data.message;
      } catch {
        // ignore
      }
      return `请求失败（${response.status}）`;
    }
    const text = await response.text().catch(() => "");
    try {
      const data = JSON.parse(text);
      if (data && typeof data.msg === "string") return data.msg;
      if (data && typeof data.message === "string") return data.message;
    } catch {
      const trimmed = (text || "").trim();
      if (trimmed && !trimmed.startsWith("<")) {
        return trimmed.length > 200 ? `${trimmed.slice(0, 200)}...` : trimmed;
      }
    }
    return `请求失败（${response.status}）`;
  }

  try {
    // 验证数据
    const validatedData = registerFormSchema.parse({
      username: formData.get("username"),
      email: formData.get("email"),
      password: formData.get("password"),
      confirmPassword: formData.get("confirmPassword"),
      nickname: formData.get("nickname") || undefined,
      captchaUuid: formData.get("captchaUuid"),
      captchaCode: formData.get("captchaCode"),
    });

    const response = await apiFetch(`${PLATFORM_AUTH_BASE_URL}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(validatedData),
    });

    if (!response.ok) {
      const message = await extractErrorMessage(response);
      return { status: "failed", message };
    }

    const result = (await response.json()) as ApiResponse<unknown>;
    if (response.ok && result.code === 200) {
      return { status: "success" };
    }

    const message = result.msg || "注册失败";
    if (message.includes("存在")) {
      return { status: "user_exists", message };
    }
    return { status: "failed", message };
  } catch (error) {
    // 验证数据无效
    if (error instanceof z.ZodError) {
      return {
        status: "invalid_data",
        message: error.issues[0]?.message ?? "提交的数据无效",
      };
    }

    return { status: "failed", message: error instanceof Error ? error.message : "注册失败" };
  }
};
