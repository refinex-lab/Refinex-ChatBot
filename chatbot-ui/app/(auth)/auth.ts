/**
 * 认证模块
 */
import NextAuth, {type DefaultSession} from "next-auth";
import type {DefaultJWT} from "next-auth/jwt";
import Credentials from "next-auth/providers/credentials";
import {cookies} from "next/headers";

import {createGuestUser} from "@/lib/db/queries";
import {PLATFORM_AUTH_BASE_URL} from "@/lib/env";
import type {ApiResponse} from "@/lib/types/api";
import {authConfig} from "./auth.config";

/**
 * 用户类型 guest(访客) | regular(注册用户)
 */
export type UserType = "guest" | "regular";

/**
 * 平台登录凭据类型
 */
type PlatformLoginCredentials = {
  email: string;
  password: string;
  captchaUuid?: string;
  captchaCode?: string;
  rememberMe?: string | boolean;
  deviceType?: string;
};

/**
 * 平台登录用户类型
 */
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

/**
 * 平台登录响应类型
 */
type PlatformLoginResponse = {
  tokenName: string;
  tokenValue: string;
  expireIn: number;
  user: PlatformLoginUser;
};

/**
 * 声明模块
 */
declare module "next-auth" {
  // 会话接口
  interface Session extends DefaultSession {
    user: {
      id: string;
      type: UserType;
      roles?: string[];
      permissions?: string[];
      tokenName?: string;
      tokenValue?: string;
    } & DefaultSession["user"];
  }

  // 用户接口
  // biome-ignore lint/nursery/useConsistentTypeDefinitions: "Required"
  interface User {
    id?: string;
    email?: string | null;
    type: UserType;
    roles?: string[];
    permissions?: string[];
    tokenName?: string;
    tokenValue?: string;
    avatar?: string | null;
  }
}

/**
 * 声明模块
 */
declare module "next-auth/jwt" {
  // JWT 接口
  interface JWT extends DefaultJWT {
    id: string;
    type: UserType;
    roles?: string[];
    permissions?: string[];
    tokenName?: string;
    tokenValue?: string;
    avatar?: string | null;
  }
}

/**
 * 认证模块
 */
export const {
  handlers: { GET, POST },
  auth,
  signIn,
  signOut,
} = NextAuth({
  ...authConfig,
  providers: [
    Credentials({
      credentials: {},
      async authorize(credentials) {
        const { email, password, captchaUuid, captchaCode, rememberMe, deviceType } =
          (credentials ?? {}) as PlatformLoginCredentials;

        if (!email || !password) {
          return null;
        }

        const response = await fetch(`${PLATFORM_AUTH_BASE_URL}/login`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email,
            password,
            captchaUuid,
            captchaCode,
            rememberMe:
              typeof rememberMe === "string"
                ? rememberMe === "true" || rememberMe === "on"
                : Boolean(rememberMe),
            deviceType: deviceType ?? "PC",
          }),
        });

        if (!response.ok) {
          throw new Error("登录失败");
        }

        const result =
          (await response.json()) as ApiResponse<PlatformLoginResponse | null>;

        if (result.code !== 200 || !result.data) {
          throw new Error(result.msg || "登录失败");
        }

        const loginData = result.data;
        const user = loginData.user;

        if (!user) {
          throw new Error("登录失败");
        }

        const tokenName = loginData.tokenName || "satoken";
        const tokenValue = loginData.tokenValue;
        const expires = Math.max(60, loginData.expireIn || 0);

        try {
          const cookieStore = await cookies();
          cookieStore.set(tokenName, tokenValue, {
            httpOnly: true,
            sameSite: "lax",
            secure: process.env.NODE_ENV === "production",
            path: "/",
            maxAge: expires,
          });
        } catch {
          // ignore cookie errors
        }

        return {
          id: String(user.userId ?? user.username ?? email),
          email: user.email ?? email,
          name: user.nickname ?? user.username ?? email,
          type: "regular" as UserType,
          roles: user.roles ?? [],
          permissions: user.permissions ?? [],
          tokenName,
          tokenValue,
          avatar: user.avatar ?? null,
        };
      },
    }),

    /**
     * 访客提供者
     */
    Credentials({
      // 提供者 ID
      id: "guest",
      credentials: {},
      async authorize() {
        // 创建访客用户
        const [guestUser] = await createGuestUser();
        // 返回访客用户
        return { ...guestUser, type: "guest" };
      },
    }),
  ],
  callbacks: {
    jwt({ token, user }) {
      // 如果用户存在
      if (user) {
        token.id = user.id as string;
        // 设置用户类型
        token.type = user.type;
        token.roles = user.roles ?? [];
        token.permissions = user.permissions ?? [];
        token.tokenName = user.tokenName;
        token.tokenValue = user.tokenValue;
        token.avatar = user.avatar ?? null;
      }

      return token;
    },
    session({ session, token }) {
      if (session.user) {
        // 设置用户 ID
        session.user.id = token.id;
        // 设置用户类型
        session.user.type = token.type;
        session.user.roles = token.roles ?? [];
        session.user.permissions = token.permissions ?? [];
        session.user.tokenName = token.tokenName;
        session.user.tokenValue = token.tokenValue;
        session.user.image = token.avatar ?? session.user.image;
      }

      return session;
    },
  },
});
