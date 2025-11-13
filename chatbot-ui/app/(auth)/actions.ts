/**
 * 认证动作
 */
"use server";

import {AuthError} from "next-auth";
import {z} from "zod";

import {PLATFORM_AUTH_BASE_URL} from "@/lib/env";
import type {ApiResponse} from "@/lib/types/api";

import {signIn} from "./auth";

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
  try {
    // 验证数据
    const validatedData = loginFormSchema.parse({
      email: formData.get("email"),
      password: formData.get("password"),
      captchaUuid: formData.get("captchaUuid") || undefined,
      captchaCode: formData.get("captchaCode") || undefined,
      rememberMe: formData.get("rememberMe") === "on",
    });

    // 登录
    await signIn("credentials", {
      email: validatedData.email,
      password: validatedData.password,
      captchaUuid: validatedData.captchaUuid,
      captchaCode: validatedData.captchaCode,
      rememberMe: String(Boolean(validatedData.rememberMe)),
      deviceType: "PC",
      redirect: false,
    });

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

    if (error instanceof AuthError) {
        let message: string;

        if (typeof (error.cause as any)?.message === "string") {
            message = (error.cause as any).message;
        } else if (typeof error.message === "string") {
            message = error.message;
        } else {
            message = "登录失败";
        }

      return {
        status: "failed",
        message,
      };
    }
    // 返回失败状态
    return { status: "failed", message: "登录失败，请稍后再试" };
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

    const response = await fetch(`${PLATFORM_AUTH_BASE_URL}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(validatedData),
    });

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
