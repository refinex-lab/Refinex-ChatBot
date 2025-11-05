/**
 * 访客认证路由
 */
import { NextResponse } from "next/server";
import { getToken } from "next-auth/jwt";
import { signIn } from "@/app/(auth)/auth";
import { isDevelopmentEnvironment } from "@/lib/constants";

/**
 * 获取访客认证
 */
export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const redirectUrl = searchParams.get("redirectUrl") || "/";

  // 获取令牌
  const token = await getToken({
    req: request,
    secret: process.env.AUTH_SECRET,
    secureCookie: !isDevelopmentEnvironment,
  });

  // 如果令牌存在
  if (token) {
    // 重定向到首页
    return NextResponse.redirect(new URL("/", request.url));
  }

  // 登录访客
  return signIn("guest", { redirect: true, redirectTo: redirectUrl });
}
