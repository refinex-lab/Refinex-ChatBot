"use client";

import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useRouter} from "next/navigation";
import * as Dialog from "@radix-ui/react-dialog";
import * as VisuallyHidden from "@radix-ui/react-visually-hidden";
import {IoCreateOutline} from "react-icons/io5";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {useSidebar} from "@/components/ui/sidebar";
import type {Chat} from "@/lib/db/schema";
import {cn} from "@/lib/utils";

export interface SidebarSearchDialogProps {
  visible: boolean;
  onClose: () => void;
  chats: Chat[];
}

export const SidebarSearchDialog = ({
  visible,
  onClose,
  chats,
}: SidebarSearchDialogProps) => {
  const router = useRouter();
  const {setOpenMobile} = useSidebar();
  const [searchQuery, setSearchQuery] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  // 过滤聊天记录
  const filteredChats = useMemo(() => {
    if (!searchQuery.trim()) {
      return [];
    }
    const query = searchQuery.toLowerCase().trim();
    return chats.filter((chat) =>
      chat.title.toLowerCase().includes(query)
    );
  }, [chats, searchQuery]);

  // 处理新聊天
  const handleNewChat = useCallback(() => {
    onClose();
    setOpenMobile(false);
    router.push("/");
    router.refresh();
  }, [onClose, router, setOpenMobile]);

  // 处理搜索结果点击
  const handleChatClick = useCallback(
    (chatId: string) => {
      onClose();
      setOpenMobile(false);
      router.push(`/chat/${chatId}`);
    },
    [onClose, router, setOpenMobile]
  );

  // 处理键盘事件
  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      }
    },
    [onClose]
  );

  // 自动聚焦搜索框
  useEffect(() => {
    if (visible && inputRef.current) {
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
    } else {
      setSearchQuery("");
    }
  }, [visible]);

  return (
    <Dialog.Root open={visible} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 z-50 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0" />
        <Dialog.Content
          className={cn(
            "fixed left-[50%] top-[50%] z-50 grid w-full max-w-lg translate-x-[-50%] translate-y-[-50%] gap-4 border bg-background p-6 shadow-lg duration-200 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[state=closed]:slide-out-to-left-1/2 data-[state=closed]:slide-out-to-top-[48%] data-[state=open]:slide-in-from-left-1/2 data-[state=open]:slide-in-from-top-[48%] rounded-lg"
          )}
          onKeyDown={handleKeyDown}
        >
          <VisuallyHidden.Root>
            <Dialog.Title>搜索聊天记录</Dialog.Title>
          </VisuallyHidden.Root>

          {/* 搜索框 */}
          <div className="flex flex-col gap-4">
            <Input
              ref={inputRef}
              placeholder="搜索聊天记录..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full"
            />

            {/* 新聊天快捷入口 */}
            <Button
              type="button"
              variant="outline"
              onClick={handleNewChat}
              className="w-full justify-start gap-2"
            >
              <IoCreateOutline size={16} />
              <span>新聊天</span>
            </Button>

            {/* 搜索结果列表 */}
            {searchQuery.trim() && (
              <div className="max-h-[400px] overflow-y-auto">
                {filteredChats.length > 0 ? (
                  <div className="flex flex-col gap-1">
                    {filteredChats.map((chat) => (
                      <div
                        key={chat.id}
                        className="rounded-md hover:bg-accent cursor-pointer"
                        onClick={() => handleChatClick(chat.id)}
                        onKeyDown={(e) => {
                          if (e.key === "Enter" || e.key === " ") {
                            e.preventDefault();
                            handleChatClick(chat.id);
                          }
                        }}
                        role="button"
                        tabIndex={0}
                      >
                        <div className="px-3 py-2 text-sm">{chat.title}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex items-center justify-center py-8 text-sm text-muted-foreground">
                    未找到匹配的聊天记录
                  </div>
                )}
              </div>
            )}

            {/* 空状态提示 */}
            {!searchQuery.trim() && (
              <div className="flex items-center justify-center py-8 text-sm text-muted-foreground">
                输入关键词搜索聊天记录
              </div>
            )}
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
};

