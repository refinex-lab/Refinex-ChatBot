/**
 * 设置弹窗（左侧导航 + 右侧详情）
 */
"use client";

import {Dialog, DialogClose, DialogContent, DialogTitle} from "@/components/ui/dialog";
import {AccountSettings} from "@/components/settings/account-settings";
import {GeneralSettings} from "@/components/settings/general-settings";
import {DataSettings} from "@/components/settings/data-settings";
import {StorageSettings} from "@/components/settings/storage-settings";
import {PromptSettings} from "@/components/settings/prompt-settings";
import {useState} from "react";
import {X} from "lucide-react";
import {CiSettings} from "react-icons/ci";
import {TbDatabaseStar} from "react-icons/tb";
import {RiAccountCircleLine} from "react-icons/ri";
import {MdOutlineCloudDownload, MdOutlineTipsAndUpdates} from "react-icons/md";

export function SettingsDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  // 当前选中的设置项
  const [active, setActive] = useState<"general" | "data" | "storage" | "prompt" | "account">("account");

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="h-[80vh]">
        {/* a11y: 必须提供 DialogTitle，但不占据布局空间 */}
        <DialogTitle className="sr-only absolute -left-[9999px] -top-[9999px]">设置</DialogTitle>
        <div className="relative flex h-full flex-col">
          {/* 左上角关闭按钮 */}
          <DialogClose asChild>
            <button
              aria-label="关闭"
              className="absolute left-3 top-3 rounded-md p-1 text-muted-foreground hover:bg-accent"
            >
              <X className="size-5" />
            </button>
          </DialogClose>

          {/* 主体区域：左右结构，固定高度滚动 */}
          <div className="flex min-h-0 flex-1 overflow-hidden">
            {/* 左侧菜单 */}
            <aside className="w-56 shrink-0 border-r pt-12">
              <nav className="flex flex-col gap-1 px-2">
                <button
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm hover:bg-accent/60 data-[active=true]:bg-accent/60 data-[active=true]:ring-1 data-[active=true]:ring-[color:var(--brand-color)]"
                  data-active={active === "general"}
                  onClick={() => setActive("general")}
                >
                  <CiSettings className="size-4" />
                  <span>常规</span>
                </button>
                <button
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm hover:bg-accent/60 data-[active=true]:bg-accent/60 data-[active=true]:ring-1 data-[active=true]:ring-[color:var(--brand-color)]"
                  data-active={active === "data"}
                  onClick={() => setActive("data")}
                >
                  <TbDatabaseStar className="size-4" />
                  <span>数据管理</span>
                </button>
                <button
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm hover:bg-accent/60 data-[active=true]:bg-accent/60 data-[active=true]:ring-1 data-[active=true]:ring-[color:var(--brand-color)]"
                  data-active={active === "storage"}
                  onClick={() => setActive("storage")}
                >
                  <MdOutlineCloudDownload className="size-4" />
                  <span>存储管理</span>
                </button>
                <button
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm hover:bg-accent/60 data-[active=true]:bg-accent/60 data-[active=true]:ring-1 data-[active=true]:ring-[color:var(--brand-color)]"
                  data-active={active === "prompt"}
                  onClick={() => setActive("prompt")}
                >
                  <MdOutlineTipsAndUpdates className="size-4" />
                  <span>提示词</span>
                </button>
                <button
                  className="flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm hover:bg-accent/60 data-[active=true]:bg-accent/60 data-[active=true]:ring-1 data-[active=true]:ring-[color:var(--brand-color)]"
                  data-active={active === "account"}
                  onClick={() => setActive("account")}
                >
                  <RiAccountCircleLine className="size-4" />
                  <span>账户</span>
                </button>
              </nav>
            </aside>

            {/* 右侧详情 */}
            <div className="flex-1 min-h-0 h-full overflow-y-auto">
            {active === "general" && <GeneralSettings />}
            {active === "data" && <DataSettings onClose={() => onOpenChange(false)} />}
            {active === "storage" && <StorageSettings />}
            {active === "prompt" && <PromptSettings />}
            {active === "account" && <AccountSettings />}
          </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
