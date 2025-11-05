/**
 * 认证模块
 */
import { compare } from "bcrypt-ts";
import NextAuth, { type DefaultSession } from "next-auth";
import type { DefaultJWT } from "next-auth/jwt";
import Credentials from "next-auth/providers/credentials";
import { DUMMY_PASSWORD } from "@/lib/constants";
import { createGuestUser, getUser } from "@/lib/db/queries";
import { authConfig } from "./auth.config";

/**
 * 用户类型
 */
export type UserType = "guest" | "regular";

/**
 * 声明模块
 */
declare module "next-auth" {
  // 会话接口
  interface Session extends DefaultSession {
    user: {
      id: string;
      type: UserType;
    } & DefaultSession["user"];
  }

  // 用户接口
  // biome-ignore lint/nursery/useConsistentTypeDefinitions: "Required"
  interface User {
    id?: string;
    email?: string | null;
    type: UserType;
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
      async authorize({ email, password }: any) {
        // 获取用户
        const users = await getUser(email);

        if (users.length === 0) {
          // 比较密码
          await compare(password, DUMMY_PASSWORD);
          return null;
        }

        // 获取用户
        const [user] = users;

        // 如果没有密码
        if (!user.password) {
          // 比较密码
          await compare(password, DUMMY_PASSWORD);
          return null;
        }

        // 比较密码
        const passwordsMatch = await compare(password, user.password);

        // 如果密码不匹配
        if (!passwordsMatch) {
          // 返回空用户
          return null;
        }

        // 返回用户
        return { ...user, type: "regular" };
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
      }

      return token;
    },
    session({ session, token }) {
      if (session.user) {
        // 设置用户 ID
        session.user.id = token.id;
        // 设置用户类型
        session.user.type = token.type;
      }

      return session;
    },
  },
});
