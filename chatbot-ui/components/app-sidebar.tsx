/**
 * 应用侧边栏
 */
"use client";

import Image from "next/image";
import Link from "next/link";
import type {User} from "next-auth";
import {useTheme} from "next-themes";
import {useEffect, useState} from "react";
import useSWRInfinite from "swr/infinite";
import {SidebarExpandIcon} from "@/components/icons";
import {type ChatHistory, getChatHistoryPaginationKey, SidebarHistory} from "@/components/sidebar-history";
import {SidebarActions} from "@/components/sidebar-actions";
import {SidebarToggle} from "@/components/sidebar-toggle";
import {SidebarUserNav} from "@/components/sidebar-user-nav";
import {Sidebar, SidebarContent, SidebarFooter, SidebarHeader, SidebarMenu, useSidebar,} from "@/components/ui/sidebar";
import {fetcher} from "@/lib/utils";

/**
 * 应用侧边栏组件
 */
export function AppSidebar({ user }: { user: User | undefined }) {
  const { setOpenMobile, state, toggleSidebar } = useSidebar();
  const [isHoveringLogo, setIsHoveringLogo] = useState(false);
  const { resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  // 确保只在客户端渲染时使用主题，避免 hydration 错误
  useEffect(() => {
    setMounted(true);
  }, []);

  // 获取聊天记录用于搜索功能
  const { data: paginatedChatHistories } = useSWRInfinite<ChatHistory>(
    getChatHistoryPaginationKey,
    fetcher,
    {
      fallbackData: [],
    }
  );

  // 获取所有聊天记录
  const allChats =
    paginatedChatHistories?.flatMap(
      (paginatedChatHistory) => paginatedChatHistory.chats
    ) ?? [];

  // 返回应用侧边栏组件
  return (
    <>
      <Sidebar className="group-data-[side=left]:border-r-0" collapsible="icon">
        <SidebarHeader>
          {/* 侧边栏菜单 */}
          <SidebarMenu>
            <div className="flex flex-row items-center justify-between group-data-[collapsible=icon]:flex-col group-data-[collapsible=icon]:gap-2">
              {/* 侧边栏链接，点击后关闭移动端侧边栏并跳转到首页 */}
              <Link
                aria-label="返回首页"
                className="flex flex-row items-center gap-3 group-data-[collapsible=icon]:justify-center"
                href="/"
                onClick={(e) => {
                  // 折叠状态时，点击 Logo 展开侧边栏
                  if (state === "collapsed") {
                    e.preventDefault();
                    toggleSidebar();
                  } else {
                    setOpenMobile(false);
                  }
                }}
                onMouseEnter={() => setIsHoveringLogo(true)}
                onMouseLeave={() => setIsHoveringLogo(false)}
                title="返回首页"
              >
                {/* 系统 Logo - 折叠状态且 hover 时显示展开图标，否则显示 Logo */}
                {state === "collapsed" && isHoveringLogo ? (
                  <div className="flex h-8 w-8 items-center justify-center rounded-md p-1 transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800">
                    <SidebarExpandIcon size={24} />
                  </div>
                ) : (
                  <Image
                    alt="RefinexChatBot Logo"
                    className="h-8 w-8 cursor-pointer rounded-md p-1 transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800"
                    height={32}
                    src={mounted && resolvedTheme === "dark" ? "/images/logo-dark.svg" : "/images/logo.svg"}
                    width={32}
                  />
                )}
              </Link>
              {/* 侧边栏按钮 */}
              <div className="flex flex-row gap-1 group-data-[collapsible=icon]:flex-col">
                {/* 展开状态时显示展开/收起按钮 */}
                {state === "expanded" && <SidebarToggle />}
              </div>
            </div>
          </SidebarMenu>
          {/* 功能按钮区域 - 折叠和展开状态都显示 */}
          <SidebarActions chats={allChats} />
        </SidebarHeader>
        {/* 折叠状态下不显示消息历史 */}
        {state === "expanded" && (
          <SidebarContent>
            {/* 侧边栏历史聊天记录 */}
            <SidebarHistory user={user} />
          </SidebarContent>
        )}
        <SidebarFooter>
          {/* 用户已登录时显示用户导航 */}
          {user && <SidebarUserNav user={user} />}
        </SidebarFooter>
      </Sidebar>
    </>
  );
}
