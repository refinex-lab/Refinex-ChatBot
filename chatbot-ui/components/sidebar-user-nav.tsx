/**
 * 侧边栏用户导航
 */
"use client";

import {ChevronUp} from "lucide-react";
import {useRouter} from "next/navigation";
import {useEffect, useState} from "react";
import {CiLogout, CiSettings} from "react-icons/ci";
import {toast} from "sonner";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {SidebarMenu, SidebarMenuButton, SidebarMenuItem,} from "@/components/ui/sidebar";
import {AUTH_COOKIE_NAME} from "@/lib/env";
import {SettingsDialog} from "@/components/settings/settings-dialog";
import {useUserProfile} from "@/lib/api/user";

/**
 * 侧边栏用户导航组件
 */
type UserLike = { email?: string | null };
export function SidebarUserNav({ user }: { user: UserLike }) {
  const router = useRouter();
  const { data: profile } = useUserProfile();
  const [mounted, setMounted] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const defaultAvatar = "/images/user/default-avatar.svg";

  // 确保只在客户端渲染时使用主题，避免 hydration 错误
  useEffect(() => {
    setMounted(true);
  }, []);

  const tokenName = AUTH_COOKIE_NAME || "satoken";

  const handleSignOut = async () => {
    try {
      // 改为调用服务端代理路由，附带 Authorization 头并清理 HttpOnly Cookie
      const resp = await fetch("/api/auth/logout", { method: "POST" });
      let message = "退出登录成功";
      try {
        const data = await resp.json();
        if (!resp.ok || data?.code !== 200) {
          message = data?.msg || "退出登录失败";
          // 失败提示
          toast.error(message);
        } else {
          toast.success(message);
        }
      } catch {
        // 解析失败时的兜底提示
        if (!resp.ok) {
          toast.error("退出登录失败");
        } else {
          toast.success(message);
        }
      }
    } catch (error) {
      console.error("退出登录失败", error);
      toast.error("退出登录失败");
    } finally {
      // Cookie 已由服务端路由清理，这里仅做导航
      router.push("/login");
    }
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
                {/* 侧边栏菜单按钮 */}
                <SidebarMenuButton
                  className="h-10 bg-background data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                  data-testid="user-nav-button"
                >
                  {/* 用户头像 */}
                  <img
                    alt={profile?.nickname || user.email || "User Avatar"}
                    className="h-6 w-6 rounded-full border object-cover"
                    src={profile?.avatar || defaultAvatar}
                  />
                  {/* 用户名或邮箱 */}
                  <span className="truncate" data-testid="user-email">
                    {profile?.nickname || user?.email}
                  </span>
                  {/* 下拉菜单箭头 */}
                  <ChevronUp className="ml-auto" />
                </SidebarMenuButton>
            </DropdownMenuTrigger>
            {/* 下拉菜单内容 */}
            <DropdownMenuContent
              className="w-(--radix-popper-anchor-width)"
              data-testid="user-nav-menu"
              side="top"
            >
              {/* 设置 */}
              <DropdownMenuItem
                className="cursor-pointer"
                data-testid="user-nav-item-settings"
                onSelect={() => setShowSettings(true)}
              >
                <div className="flex items-center gap-2">
                  <CiSettings className="size-4" />
                  <span>设置</span>
                </div>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              {/* 登录/登出按钮 */}
              <DropdownMenuItem asChild data-testid="user-nav-item-auth">
                <button
                  className="flex w-full items-center gap-2 cursor-pointer"
                  onClick={handleSignOut}
                  type="button"
                >
                  <>
                    <CiLogout className="size-4" />
                    <span>登出</span>
                  </>
                </button>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>

      {/* 设置弹窗 */}
      <SettingsDialog open={showSettings} onOpenChange={setShowSettings} />
    </>
  );
}
