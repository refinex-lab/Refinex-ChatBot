/**
 * 认证动作
 */
"use server";

import { z } from "zod";

import { createUser, getUser } from "@/lib/db/queries";

import { signIn } from "./auth";

/**
 * 认证表单模式
 */
const authFormSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});

/**
 * 登录状态
 */
export type LoginActionState = {
  status: "idle" | "in_progress" | "success" | "failed" | "invalid_data";
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
    const validatedData = authFormSchema.parse({
      email: formData.get("email"),
      password: formData.get("password"),
    });

    // 登录
    await signIn("credentials", {
      email: validatedData.email,
      password: validatedData.password,
      redirect: false,
    });

    // 返回成功状态
    return { status: "success" };
  } catch (error) {
    // 验证数据无效
    if (error instanceof z.ZodError) {
      // 返回无效数据状态
      return { status: "invalid_data" };
    }

    // 返回失败状态
    return { status: "failed" };
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
    const validatedData = authFormSchema.parse({
      email: formData.get("email"),
      password: formData.get("password"),
    });

    // 获取用户
    const [user] = await getUser(validatedData.email);

    // 用户已存在
    if (user) {
      // 返回用户已存在状态
      return { status: "user_exists" } as RegisterActionState;
    }
    // 创建用户
    await createUser(validatedData.email, validatedData.password);
    // 登录
    await signIn("credentials", {
      email: validatedData.email,
      password: validatedData.password,
      redirect: false,
    });

    // 返回成功状态
    return { status: "success" };
  } catch (error) {
    // 验证数据无效
    if (error instanceof z.ZodError) {
      // 返回无效数据状态
      return { status: "invalid_data" };
    }

    // 返回失败状态
    return { status: "failed" };
  }
};
