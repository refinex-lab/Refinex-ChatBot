/**
 * 侧边栏用户导航
 */
"use client";

import {ChevronUp} from "lucide-react";
import Image from "next/image";
import {useRouter} from "next/navigation";
import type {User} from "next-auth";
import {signOut, useSession} from "next-auth/react";
import {useTheme} from "next-themes";
import {useEffect, useState} from "react";
import {BsMoonStars} from "react-icons/bs";
import {CiLogin, CiLogout, CiTrash} from "react-icons/ci";
import {FiSun} from "react-icons/fi";
import {toast} from "sonner";
import {useSWRConfig} from "swr";
import {unstable_serialize} from "swr/infinite";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {SidebarMenu, SidebarMenuButton, SidebarMenuItem,} from "@/components/ui/sidebar";
import {guestRegex} from "@/lib/constants";
import {PLATFORM_AUTH_BASE_URL} from "@/lib/env";
import {getChatHistoryPaginationKey} from "@/components/sidebar-history";
import {LoaderIcon} from "./icons";
import {toast as toastNotification} from "./toast";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "./ui/alert-dialog";

/**
 * 侧边栏用户导航组件
 */
export function SidebarUserNav({ user }: { user: User }) {
  const router = useRouter();
  const { data, status } = useSession();
  const { setTheme, resolvedTheme } = useTheme();
  const { mutate } = useSWRConfig();
  const [showDeleteAllDialog, setShowDeleteAllDialog] = useState(false);
  const [mounted, setMounted] = useState(false);

  // 确保只在客户端渲染时使用主题，避免 hydration 错误
  useEffect(() => {
    setMounted(true);
  }, []);

  // 是否为游客
  const isGuest = guestRegex.test(data?.user?.email ?? "");
  const tokenName = data?.user?.tokenName ?? "satoken";

  const handleSignOut = async () => {
    if (status === "loading") {
      toastNotification({
        type: "error",
        description: "检查认证状态中，请稍后再试！",
      });
      return;
    }

    if (isGuest) {
      router.push("/login");
      return;
    }

    try {
      await fetch(`${PLATFORM_AUTH_BASE_URL}/logout`, {
        method: "POST",
        credentials: "include",
      });
    } catch (error) {
      console.error("退出登录失败", error);
    } finally {
      document.cookie = `${tokenName}=; Max-Age=0; path=/;`;
      signOut({
        redirectTo: "/",
      });
    }
  };

  // 删除所有聊天记录
  const handleDeleteAll = () => {
    const deletePromise = fetch("/api/history", {
      method: "DELETE",
    });

    toast.promise(deletePromise, {
      loading: "删除所有聊天记录中...",
      success: () => {
        mutate(unstable_serialize(getChatHistoryPaginationKey));
        router.push("/");
        setShowDeleteAllDialog(false);
        return "所有聊天记录删除成功";
      },
      error: "所有聊天记录删除失败",
    });
  };

  return (
    <>
      {/* 侧边栏菜单 */}
      <SidebarMenu>
        {/* 侧边栏菜单项 */}
        <SidebarMenuItem>
          <DropdownMenu>
            {/* 下拉菜单触发器 */}
            <DropdownMenuTrigger asChild>
              {status === "loading" ? (
                <SidebarMenuButton className="h-10 justify-between bg-background data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground">
                  <div className="flex flex-row gap-2">
                    <div className="size-6 animate-pulse rounded-full bg-zinc-500/30" />
                    <span className="animate-pulse rounded-md bg-zinc-500/30 text-transparent">
                      加载认证状态中...
                    </span>
                  </div>
                  <div className="animate-spin text-zinc-500">
                    <LoaderIcon />
                  </div>
                </SidebarMenuButton>
              ) : (
                // 侧边栏菜单按钮
                <SidebarMenuButton
                  className="h-10 bg-background data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                  data-testid="user-nav-button"
                >
                  {/* 用户头像 */}
                  <Image
                    alt={user.email ?? "User Avatar"}
                    className="rounded-full"
                    height={24}
                    src={`https://avatar.vercel.sh/${user.email}`}
                    width={24}
                  />
                  {/* 用户邮箱 */}
                  <span className="truncate" data-testid="user-email">
                    {isGuest ? "Guest" : user?.email}
                  </span>
                  {/* 下拉菜单箭头 */}
                  <ChevronUp className="ml-auto" />
                </SidebarMenuButton>
              )}
            </DropdownMenuTrigger>
            {/* 下拉菜单内容 */}
            <DropdownMenuContent
              className="w-(--radix-popper-anchor-width)"
              data-testid="user-nav-menu"
              side="top"
            >
              {/* 切换主题 */}
              <DropdownMenuItem
                className="cursor-pointer"
                data-testid="user-nav-item-theme"
                onSelect={() =>
                  setTheme(resolvedTheme === "dark" ? "light" : "dark")
                }
              >
                <div className="flex items-center gap-2">
                  {mounted && resolvedTheme === "dark" ? (
                    <FiSun className="size-4" />
                  ) : (
                    <BsMoonStars className="size-4" />
                  )}
                  <span>{`切换${mounted && resolvedTheme === "light" ? "暗色" : "亮色"}模式`}</span>
                </div>
              </DropdownMenuItem>
              {/* 分割线 */}
              <DropdownMenuSeparator />
              {/* 删除所有聊天记录 */}
              {!isGuest && (
                <>
                  <DropdownMenuItem
                    className="cursor-pointer text-destructive focus:text-destructive"
                    data-testid="user-nav-item-delete-all"
                    onSelect={() => setShowDeleteAllDialog(true)}
                  >
                    <div className="flex items-center gap-2">
                      <CiTrash className="size-4" />
                      <span>删除所有聊天记录</span>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                </>
              )}
              {/* 登录/登出按钮 */}
              <DropdownMenuItem asChild data-testid="user-nav-item-auth">
                <button
                  className="flex w-full items-center gap-2 cursor-pointer"
                  onClick={handleSignOut}
                  type="button"
                >
                  {isGuest ? (
                    <>
                      <CiLogin className="size-4" />
                      <span>登录您的账户</span>
                    </>
                  ) : (
                    <>
                      <CiLogout className="size-4" />
                      <span>登出</span>
                    </>
                  )}
                </button>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>

      {/* 删除所有聊天记录对话框 */}
      <AlertDialog onOpenChange={setShowDeleteAllDialog} open={showDeleteAllDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>您确定要删除所有聊天记录吗？</AlertDialogTitle>
            <AlertDialogDescription>
              此操作无法撤销。这将永久删除所有您的聊天记录并将其从我们的服务器中删除。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteAll}>
              继续
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
