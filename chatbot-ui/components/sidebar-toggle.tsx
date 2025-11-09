/**
 * AI 聊天侧边栏 展开/折叠按钮
 */
import type {ComponentProps} from "react";

import {type SidebarTrigger, useSidebar} from "@/components/ui/sidebar";
import {Tooltip, TooltipContent, TooltipTrigger,} from "@/components/ui/tooltip";
import {cn} from "@/lib/utils";
import {SidebarExpandIcon} from "./icons";
import {Button} from "./ui/button";

export function SidebarToggle({
  className,
}: ComponentProps<typeof SidebarTrigger>) {
  const { toggleSidebar } = useSidebar();

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          className={cn("h-8 w-8 rounded-md p-1 border-0 bg-transparent transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800 shadow-none", className)}
          data-testid="sidebar-toggle-button"
          onClick={toggleSidebar}
          variant="ghost"
        >
          <SidebarExpandIcon size={16} />
        </Button>
      </TooltipTrigger>
      <TooltipContent align="start" className="hidden md:block">
        展开/收起
      </TooltipContent>
    </Tooltip>
  );
}
