/**
 * 认证配置
 */
import type { NextAuthConfig } from "next-auth";

/**
 * 认证配置
 */
export const authConfig = {
  // 页面配置
  pages: {
    signIn: "/login",
    newUser: "/",
  },
  // 提供者
  providers: [
    // 添加在 auth.ts 中，因为它是仅与 Node.js 兼容的 bcrypt 所需的
    // 同时，这个文件也用于非 Node.js 环境
  ],
  // 回调函数
  callbacks: {},
} satisfies NextAuthConfig;
