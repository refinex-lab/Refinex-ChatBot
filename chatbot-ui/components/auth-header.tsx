/**
 * 认证页面头部组件
 * 展示系统 Logo 和标题
 */
"use client";

import Image from "next/image";
import Link from "next/link";

/**
 * 认证头部组件属性
 */
interface AuthHeaderProps {
  // 应用名称，默认从环境变量读取
  appName?: string;
}

/**
 * 认证头部组件
 * 在登录、注册等认证页面的左上角展示 Logo 和标题
 * @param {AuthHeaderProps} props - 组件属性
 * @returns {JSX.Element} 认证头部组件
 */
export function AuthHeader({
  appName = process.env.NEXT_PUBLIC_APP_NAME || "RefinexChatBot",
}: AuthHeaderProps) {
  return (
    <Link
      aria-label={`返回 ${appName} 首页`}
      className="flex items-center gap-3 text-zinc-900 no-underline transition-opacity hover:opacity-80 dark:text-zinc-50"
      href="/"
      title={`返回 ${appName} 首页`}
    >
      {/* Logo 图标 */}
      <Image
        alt={`${appName} Logo`}
        className="h-8 w-8"
        height={32}
        priority
        src="/images/logo.svg"
        width={32}
      />

      {/* 站点标题 */}
      <span className="font-bold text-xl">{appName}</span>
    </Link>
  );
}

