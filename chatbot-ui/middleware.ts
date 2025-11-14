import {type NextRequest, NextResponse} from "next/server";
import {AUTH_COOKIE_NAME} from "./lib/env";

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  /*
   * Playwright starts the dev server and requires a 200 status to
   * begin the tests, so this ensures that the tests can start
   */
  if (pathname.startsWith("/ping")) {
    return new Response("pong", { status: 200 });
  }

  // 无 next-auth 相关路由，直接走默认逻辑

  // 兼容后端登录态：检测 Sa-Token Cookie 是否存在
  const isAuthenticated =
    Boolean(request.cookies.get(AUTH_COOKIE_NAME)?.value) ||
    Boolean(request.cookies.get("satoken")?.value);

  if (!isAuthenticated) {
    // 未认证：允许访问登录/注册页与公共资源，避免重定向循环
    if (
      pathname === "/login" ||
      pathname === "/register" ||
      pathname.startsWith("/_next") ||
      pathname.startsWith("/images") ||
      pathname === "/favicon.ico" ||
      pathname === "/robots.txt" ||
      pathname === "/sitemap.xml"
    ) {
      return NextResponse.next();
    }

    const redirectUrl = encodeURIComponent(request.url);
    // 未认证：统一跳转登录页（不再支持访客模式）
    return NextResponse.redirect(new URL(`/login?redirectUrl=${redirectUrl}`, request.url));
  }

  // 已登录（后端或 NextAuth 会话），不再访问登录/注册页
  if (isAuthenticated && ["/login", "/register"].includes(pathname)) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/",
    "/chat/:id",
    "/api/:path*",
    "/login",
    "/register",

    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico, sitemap.xml, robots.txt (metadata files)
     */
    "/((?!_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)",
  ],
};
