"use client";

import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuLightbulb, LuPenLine, LuPlus, LuRefreshCcw, LuTrash2} from "react-icons/lu";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Switch} from "@/components/ui/switch";
import {Textarea} from "@/components/ui/textarea";
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {ScrollArea} from "@/components/ui/scroll-area";
import {
    AdvisorMutationPayload,
    AdvisorQuery,
    AiAdvisor,
    createAdvisor,
    deleteAdvisor,
    queryAdvisors,
    updateAdvisor,
    updateAdvisorStatus,
} from "@/lib/api/ai/advisor";

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; advisor: AiAdvisor };

type AdvisorFormState = {
  advisorCode: string;
  advisorName: string;
  advisorType: string;
  sort: string;
  status: boolean;
  remark: string;
};

function toFormState(advisor?: AiAdvisor): AdvisorFormState {
  if (!advisor) {
    return {
      advisorCode: "",
      advisorName: "",
      advisorType: "",
      sort: "",
      status: true,
      remark: "",
    };
  }
  return {
    advisorCode: advisor.advisorCode ?? "",
    advisorName: advisor.advisorName ?? "",
    advisorType: advisor.advisorType ?? "",
    sort: advisor.sort?.toString() ?? "",
    status: (advisor.status ?? 1) === 1,
    remark: advisor.remark ?? "",
  };
}

function toPayload(form: AdvisorFormState): AdvisorMutationPayload {
  return {
    advisorCode: form.advisorCode.trim(),
    advisorName: form.advisorName.trim(),
    advisorType: form.advisorType.trim(),
    sort: form.sort ? Number(form.sort) : undefined,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
}

export function AdvisorSettings() {
  const [filters, setFilters] = useState({ keyword: "", advisorType: "", status: "all" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiAdvisor | null>(null);
  const [statusPendingId, setStatusPendingId] = useState<number | null>(null);

  const queryParams: AdvisorQuery = useMemo(
    () => ({
      keyword: filters.keyword || undefined,
      advisorType: filters.advisorType || undefined,
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    }),
    [filters, pagination]
  );

  const swrKey = useMemo(() => ["ai-advisors", queryParams] as const, [queryParams]);
  const { data, error, isLoading, mutate, isValidating } = useSWR(swrKey, () => queryAdvisors(queryParams));

  const advisors = data?.records ?? [];
  const totalPages = data?.pages ?? 1;

  const handleFilter = (key: keyof typeof filters, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPagination((prev) => ({ ...prev, current: 1 }));
  };

  const handlePaginate = (dir: "prev" | "next") => {
    setPagination((prev) => {
      const next = dir === "prev" ? prev.current - 1 : prev.current + 1;
      if (next < 1 || next > totalPages) return prev;
      return { ...prev, current: next };
    });
  };

  const handleSubmit = async (form: AdvisorFormState) => {
    const payload = toPayload(form);
    if (!payload.advisorCode || !payload.advisorName || !payload.advisorType) {
      toast.error("请完善必填字段");
      return;
    }
    try {
      if (editor?.mode === "edit") {
        await updateAdvisor(editor.advisor.id, payload);
        toast.success(`已更新 Advisor ${editor.advisor.advisorName}`);
      } else {
        await createAdvisor(payload);
        toast.success("已创建 Advisor");
      }
      setEditor(null);
      mutate();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "保存失败");
    }
  };

  const handleDelete = async () => {
    if (!pendingDelete) return;
    try {
      await deleteAdvisor(pendingDelete.id);
      toast.success(`已删除 ${pendingDelete.advisorName}`);
      setPendingDelete(null);
      mutate();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "删除失败");
    }
  };

  return (
    <div className="flex h-full flex-col">
      <div className="border-b px-6 py-3">
        <div className="flex items-center gap-2 text-lg font-semibold">
          <LuLightbulb className="size-5 text-primary" />
          Advisor 管理
        </div>
        <p className="text-xs text-muted-foreground">Advisor 定义了 Agent 的行为插件，可实现安全、合规、RAG 等增强逻辑。</p>
      </div>

      <div className="flex-1 space-y-6 overflow-y-auto px-6 py-6">
        <div className="space-y-3 rounded-2xl border bg-background/60 p-4 shadow-sm">
          <div className="flex flex-wrap items-center gap-3">
            <Input
              className="min-w-[200px] flex-1"
              placeholder="搜索编码/名称…"
              value={filters.keyword}
              onChange={(e) => handleFilter("keyword", e.target.value)}
            />
            <Input
              className="w-48"
              placeholder="Advisor 类型，如 moderation"
              value={filters.advisorType}
              onChange={(e) => handleFilter("advisorType", e.target.value)}
            />
            <div className="flex items-center gap-2 rounded-full border bg-muted px-2 py-1 text-xs">
              {STATUS_FILTERS.map((item) => (
                <button
                  key={item.value}
                  className={`rounded-full px-3 py-1 transition ${
                    filters.status === item.value ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground"
                  }`}
                  onClick={() => handleFilter("status", item.value)}
                >
                  {item.label}
                </button>
              ))}
            </div>
            <Button variant="outline" size="sm" onClick={() => mutate()} className="gap-1">
              <LuRefreshCcw className={`size-4 ${isValidating ? "animate-spin" : ""}`} />
              刷新
            </Button>
            <Button size="sm" className="gap-1" onClick={() => setEditor({ mode: "create" })}>
              <LuPlus className="size-4" />
              新建 Advisor
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">可根据排序控制执行先后顺序，常见类型如「RAG」「SafeGuard」「ToolRouter」。</p>
        </div>

        <div className="rounded-2xl border bg-card/60 shadow-sm">
          {error && (
            <div className="px-6 py-10 text-center text-sm text-destructive">
              加载失败：{error instanceof Error ? error.message : "未知错误"}
            </div>
          )}
          {!error && (
            <ScrollArea className="h-[420px]">
              <table className="w-full table-fixed border-collapse text-sm">
                <thead>
                  <tr className="sticky top-0 bg-muted/50 text-left text-xs uppercase text-muted-foreground">
                    <th className="w-[35%] px-5 py-3 font-medium">Advisor</th>
                    <th className="w-[20%] px-5 py-3 font-medium">类型</th>
                    <th className="w-[15%] px-5 py-3 text-center font-medium">排序</th>
                    <th className="w-[15%] px-5 py-3 text-center font-medium">状态</th>
                    <th className="w-[15%] px-5 py-3 text-center font-medium">操作</th>
                  </tr>
                </thead>
                <tbody>
                  {isLoading && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        加载中…
                      </td>
                    </tr>
                  )}
                  {!isLoading && advisors.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无 Advisor
                      </td>
                    </tr>
                  )}
                  {advisors.map((advisor) => (
                    <tr key={advisor.id} className="border-t border-border/60">
                      <td className="px-5 py-3">
                        <div className="space-y-1">
                          <div className="font-medium">{advisor.advisorName}</div>
                          <div className="text-xs text-muted-foreground">{advisor.advisorCode}</div>
                          {advisor.remark && <p className="text-xs text-muted-foreground line-clamp-2">{advisor.remark}</p>}
                        </div>
                      </td>
                      <td className="px-5 py-3 text-sm text-muted-foreground">{advisor.advisorType || "-"}</td>
                      <td className="px-5 py-3 text-center text-sm text-muted-foreground">{advisor.sort ?? "-"}</td>
                      <td className="px-5 py-3 text-center">
                        <Switch
                          checked={advisor.status === 1}
                          disabled={statusPendingId === advisor.id}
                          onCheckedChange={async (checked) => {
                            setStatusPendingId(advisor.id);
                            try {
                              await updateAdvisorStatus(advisor.id, checked ? 1 : 0);
                              toast.success(`已${checked ? "启用" : "停用"} ${advisor.advisorName}`);
                              mutate();
                            } catch (err) {
                              toast.error(err instanceof Error ? err.message : "更新状态失败");
                            } finally {
                              setStatusPendingId(null);
                            }
                          }}
                        />
                      </td>
                      <td className="px-5 py-3">
                        <div className="flex items-center justify-center gap-2">
                          <Button variant="ghost" size="icon" onClick={() => setEditor({ mode: "edit", advisor })}>
                            <LuPenLine className="size-4" />
                          </Button>
                          <Button variant="ghost" size="icon" onClick={() => setPendingDelete(advisor)}>
                            <LuTrash2 className="size-4" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </ScrollArea>
          )}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-3 text-sm text-muted-foreground">
            <Button variant="outline" size="sm" onClick={() => handlePaginate("prev")} disabled={pagination.current === 1}>
              上一页
            </Button>
            <span>
              第 {pagination.current} / {totalPages} 页
            </span>
            <Button variant="outline" size="sm" onClick={() => handlePaginate("next")} disabled={pagination.current === totalPages}>
              下一页
            </Button>
          </div>
        )}
      </div>

      <AdvisorEditorDialog editor={editor} onClose={() => setEditor(null)} onSubmit={handleSubmit} />

      <AlertDialog open={Boolean(pendingDelete)} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除 Advisor ？</AlertDialogTitle>
            <AlertDialogDescription>
              删除后绑定该 Advisor 的 Agent 将失去对应能力，是否确认删除 <span className="font-semibold">{pendingDelete?.advisorName}</span>？
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive hover:bg-destructive/90">
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

function AdvisorEditorDialog({
  editor,
  onClose,
  onSubmit,
}: {
  editor: EditorState | null;
  onClose: () => void;
  onSubmit: (form: AdvisorFormState) => void;
}) {
  const [form, setForm] = useState<AdvisorFormState>(toFormState());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm(toFormState(editor && editor.mode === "edit" ? editor.advisor : undefined));
    setSaving(false);
  }, [editor]);

  const open = Boolean(editor);

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="max-h-[80vh] overflow-y-auto p-5">
        <DialogHeader>
          <DialogTitle>{editor?.mode === "edit" ? "编辑 Advisor" : "新建 Advisor"}</DialogTitle>
        </DialogHeader>
        <form
          className="space-y-4 pt-2 "
          onSubmit={async (e) => {
            e.preventDefault();
            setSaving(true);
            try {
              await onSubmit(form);
            } finally {
              setSaving(false);
            }
          }}
        >
          <div className="grid grid-cols-2 gap-4">
            <Field label="Advisor 编码 *">
              <Input value={form.advisorCode} required onChange={(e) => setForm((prev) => ({ ...prev, advisorCode: e.target.value }))} />
            </Field>
            <Field label="Advisor 名称 *">
              <Input value={form.advisorName} required onChange={(e) => setForm((prev) => ({ ...prev, advisorName: e.target.value }))} />
            </Field>
            <Field label="Advisor 类型 *">
              <Input value={form.advisorType} required onChange={(e) => setForm((prev) => ({ ...prev, advisorType: e.target.value }))} />
            </Field>
            <Field label="排序（越小越靠前）">
              <Input type="number" value={form.sort} onChange={(e) => setForm((prev) => ({ ...prev, sort: e.target.value }))} />
            </Field>
            <Field label="启用状态">
              <div className="flex items-center gap-2 rounded-md border px-3 py-2">
                <Switch checked={form.status} onCheckedChange={(checked) => setForm((prev) => ({ ...prev, status: checked }))} />
                <span className="text-sm text-muted-foreground">{form.status ? "启用" : "停用"}</span>
              </div>
            </Field>
          </div>

          <Field label="备注">
            <Textarea rows={3} value={form.remark} onChange={(e) => setForm((prev) => ({ ...prev, remark: e.target.value }))} />
          </Field>

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="outline" onClick={onClose}>
              取消
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? "保存中…" : "保 存"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="space-y-1 text-sm">
      <span className="text-muted-foreground">{label}</span>
      {children}
    </label>
  );
}
