/**
 * 数据管理设置：删除所有聊天
 */
"use client";

import {useRouter} from "next/navigation";
import {toast} from "sonner";
import {useSWRConfig} from "swr";
import {unstable_serialize} from "swr/infinite";
import {Button} from "@/components/ui/button";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle
} from "@/components/ui/alert-dialog";
import {useState} from "react";
import {getChatHistoryPaginationKey} from "@/components/sidebar-history";

const Row = ({ label, children }: { label: string; children: React.ReactNode }) => (
  <div className="grid grid-cols-[160px_1fr] items-center gap-4 border-b px-6 py-4 last:border-b-0">
    <div className="text-sm text-muted-foreground">{label}</div>
    <div className="flex w-full items-center justify-end gap-3">{children}</div>
  </div>
);

export function DataSettings({ onClose }: { onClose?: () => void }) {
  const router = useRouter();
  const { mutate } = useSWRConfig();
  const [openConfirm, setOpenConfirm] = useState(false);

  const handleDeleteAll = () => {
    const deleting = fetch("/api/history", { method: "DELETE" });
    toast.promise(deleting, {
      loading: "删除所有聊天中...",
      success: () => {
        mutate(unstable_serialize(getChatHistoryPaginationKey));
        router.push("/");
        onClose?.();
        return "已删除所有聊天";
      },
      error: "删除失败",
    });
  };

  return (
    <div>
      <div className="border-b px-6 py-3">
        <h2 className="text-lg font-semibold">数据管理</h2>
      </div>

      <Row label="删除所有聊天">
        <Button variant="destructive" size="sm" onClick={() => setOpenConfirm(true)}>
          全部删除
        </Button>
      </Row>

      <AlertDialog open={openConfirm} onOpenChange={setOpenConfirm}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除所有聊天？</AlertDialogTitle>
            <AlertDialogDescription>该操作不可撤销，所有聊天将被永久删除。</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={handleDeleteAll}>确认删除</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
