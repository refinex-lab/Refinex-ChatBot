"use client";

import {useCallback, useState} from "react";
import {useRouter} from "next/navigation";
import {IoCreateOutline, IoSearchOutline, IoVideocamOutline} from "react-icons/io5";
import {FaRegImages} from "react-icons/fa";
import {LuBookOpen} from "react-icons/lu";
import {toast} from "sonner";
import {SidebarMenuButton, useSidebar} from "@/components/ui/sidebar";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {SidebarSearchDialog} from "./sidebar-search-dialog";
import type {Chat} from "@/lib/db/schema";

export interface SidebarActionsProps {
  chats: Chat[];
}

export const SidebarActions = ({chats}: SidebarActionsProps) => {
  const router = useRouter();
  const {setOpenMobile} = useSidebar();
  const [searchDialogOpen, setSearchDialogOpen] = useState(false);

  // 新聊天
  const handleNewChat = useCallback(() => {
    setOpenMobile(false);
    router.push("/");
    router.refresh();
  }, [router, setOpenMobile]);

  // 搜索聊天
  const handleSearch = useCallback(() => {
    setSearchDialogOpen(true);
  }, []);

  // 图像生成
  const handleImageGeneration = useCallback(() => {
    toast.info("敬请期待");
  }, []);

  // 视频生成
  const handleVideoGeneration = useCallback(() => {
    toast.info("敬请期待");
  }, []);

  // 知识库
  const handleKnowledgeBase = useCallback(() => {
    toast.info("敬请期待");
  }, []);

  return (
    <>
      <TooltipProvider>
        <div className="flex flex-col gap-1 px-2">
          <Tooltip>
            <TooltipTrigger asChild>
              <SidebarMenuButton
                onClick={handleNewChat}
                className="w-full justify-start gap-2 font-medium"
              >
                <IoCreateOutline size={16} />
                <span>新聊天</span>
              </SidebarMenuButton>
            </TooltipTrigger>
            <TooltipContent>新聊天</TooltipContent>
          </Tooltip>

          <Tooltip>
            <TooltipTrigger asChild>
              <SidebarMenuButton
                onClick={handleSearch}
                className="w-full justify-start gap-2 font-medium"
              >
                <IoSearchOutline size={16} />
                <span>搜索聊天</span>
              </SidebarMenuButton>
            </TooltipTrigger>
            <TooltipContent>搜索聊天</TooltipContent>
          </Tooltip>

          <Tooltip>
            <TooltipTrigger asChild>
              <SidebarMenuButton
                onClick={handleImageGeneration}
                className="w-full justify-start gap-2 font-medium"
              >
                <FaRegImages size={16} />
                <span>图像生成</span>
              </SidebarMenuButton>
            </TooltipTrigger>
            <TooltipContent>图像生成</TooltipContent>
          </Tooltip>

          <Tooltip>
            <TooltipTrigger asChild>
              <SidebarMenuButton
                onClick={handleVideoGeneration}
                className="w-full justify-start gap-2 font-medium"
              >
                <IoVideocamOutline size={16} />
                <span>视频生成</span>
              </SidebarMenuButton>
            </TooltipTrigger>
            <TooltipContent>视频生成</TooltipContent>
          </Tooltip>

          <Tooltip>
            <TooltipTrigger asChild>
              <SidebarMenuButton
                onClick={handleKnowledgeBase}
                className="w-full justify-start gap-2 font-medium"
              >
                <LuBookOpen size={16} />
                <span>知识库</span>
              </SidebarMenuButton>
            </TooltipTrigger>
            <TooltipContent>知识库</TooltipContent>
          </Tooltip>
        </div>
      </TooltipProvider>

      <SidebarSearchDialog
        visible={searchDialogOpen}
        onClose={() => setSearchDialogOpen(false)}
        chats={chats}
      />
    </>
  );
};

