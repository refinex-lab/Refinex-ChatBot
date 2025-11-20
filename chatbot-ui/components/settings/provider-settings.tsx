"use client";

import {type ReactNode, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuCopy, LuPenLine, LuPlus, LuRefreshCcw, LuTrash2} from "react-icons/lu";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
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
import {Label} from "@/components/ui/label";
import {ScrollArea} from "@/components/ui/scroll-area";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {
    AiProvider,
    createProvider,
    deleteProvider,
    ProviderMutationPayload,
    queryProviders,
    updateProvider,
    updateProviderStatus,
} from "@/lib/api/ai/provider";

const PROVIDER_TYPES = [
  { value: "all", label: "全部类型" },
  { value: "public", label: "公共云" },
  { value: "private", label: "专有私有" },
  { value: "self_hosted", label: "自托管" },
];

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; provider: AiProvider };

type ProviderFormState = {
  providerCode: string;
  providerName: string;
  providerType: string;
  baseUrl: string;
  apiKeyCipher: string;
  apiKeyIndex: string;
  rateLimitQpm: string;
  status: boolean;
  remark: string;
};

function toFormState(provider?: AiProvider): ProviderFormState {
  if (!provider) {
    return {
      providerCode: "",
      providerName: "",
      providerType: "public",
      baseUrl: "",
      apiKeyCipher: "",
      apiKeyIndex: "",
      rateLimitQpm: "",
      status: true,
      remark: "",
    };
  }
  return {
    providerCode: provider.providerCode ?? "",
    providerName: provider.providerName ?? "",
    providerType: provider.providerType ?? "public",
    baseUrl: provider.baseUrl ?? "",
    apiKeyCipher: provider.apiKeyCipher ?? "",
    apiKeyIndex: provider.apiKeyIndex ?? "",
    rateLimitQpm: provider.rateLimitQpm?.toString() ?? "",
    status: (provider.status ?? 1) === 1,
    remark: provider.remark ?? "",
  };
}

function toPayload(form: ProviderFormState): ProviderMutationPayload {
  return {
    providerCode: form.providerCode.trim(),
    providerName: form.providerName.trim(),
    providerType: form.providerType,
    baseUrl: form.baseUrl.trim() || undefined,
    apiKeyCipher: form.apiKeyCipher.trim() || undefined,
    apiKeyIndex: form.apiKeyIndex.trim() || undefined,
    rateLimitQpm: form.rateLimitQpm ? Number(form.rateLimitQpm) : undefined,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
}

export function ProviderSettings() {
  const [filters, setFilters] = useState({ keyword: "", providerType: "all", status: "all" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiProvider | null>(null);

  const swrKey = useMemo(() => ["ai-providers", filters, pagination] as const, [filters, pagination]);
  const { data, isLoading, error, mutate, isValidating } = useSWR(swrKey, () =>
    queryProviders({
      keyword: filters.keyword || undefined,
      providerType: filters.providerType === "all" ? undefined : filters.providerType,
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    })
  );

  const providers = data?.records ?? [];
  const total = data?.total ?? 0;
  const totalPages = data?.pages ?? 1;

  const handleFilter = (key: "keyword" | "providerType" | "status", value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPagination((prev) => ({ ...prev, current: 1 }));
  };

  const handlePaginate = (direction: "prev" | "next") => {
    setPagination((prev) => {
      const next = direction === "prev" ? prev.current - 1 : prev.current + 1;
      if (next < 1 || next > totalPages) return prev;
      return { ...prev, current: next };
    });
  };

  return (
    <div className="flex h-full flex-col">
      <div className="border-b px-6 py-3">
        <div className="flex items-center gap-2 text-lg font-semibold">
          {/* <LuBrain className="size-5 text-primary" /> */}
          模型供应商
        </div>
      </div>

      <div className="flex-1 space-y-6 overflow-y-auto px-6 py-6">
        <div className="space-y-3 rounded-2xl border bg-background/60 p-4 shadow-sm">
          <div className="flex flex-wrap items-center gap-3">
            <Input
              className="min-w-[220px] flex-1"
              placeholder="搜索供应商编码 / 名称"
              value={filters.keyword}
              onChange={(e) => handleFilter("keyword", e.target.value)}
            />
            <Select value={filters.providerType} onValueChange={(value) => handleFilter("providerType", value)}>
              <SelectTrigger className="w-44">
                <SelectValue placeholder="供应商类型" />
              </SelectTrigger>
              <SelectContent>
                {PROVIDER_TYPES.map((item) => (
                  <SelectItem key={item.value} value={item.value}>
                    {item.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
              <LuRefreshCcw className={`size-4 ${isValidating ? "animate-spin" : ""}`} /> 刷新
            </Button>
            <Button size="sm" className="gap-1" onClick={() => setEditor({ mode: "create" })}>
              <LuPlus className="size-4" /> 新建供应商
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">可填写自有 API Key 的密文，结合 KMS 或 HSM 管理密钥素材。</p>
        </div>

        <div className="rounded-2xl border bg-card/60 shadow-sm">
          {error && (
            <div className="px-6 py-10 text-center text-sm text-destructive">{error instanceof Error ? error.message : "加载失败"}</div>
          )}
          {!error && (
            <ScrollArea className="h-[420px]">
              <TooltipProvider delayDuration={200}>
                <table className="w-full table-fixed border-collapse text-sm">
                  <thead>
                    <tr className="sticky top-0 bg-muted/60 text-left text-xs uppercase text-muted-foreground">
                      <th className="w-[30%] px-5 py-3 font-medium">供应商</th>
                      <th className="w-[37%] px-5 py-3 font-medium">Base URL</th>
                      <th className="w-[15%] px-5 py-3 text-center font-medium">状态</th>
                      <th className="w-[18%] px-5 py-3 text-center font-medium">操作</th>
                    </tr>
                  </thead>
                  <tbody>
                  {isLoading && (
                    <tr>
                      <td colSpan={6} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        加载中…
                      </td>
                    </tr>
                  )}
                  {providers.length === 0 && !isLoading && (
                    <tr>
                      <td colSpan={6} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无供应商记录
                      </td>
                    </tr>
                  )}
                  {providers.map((item) => (
                    <tr key={item.id} className="border-t border-border/60">
                      <td className="px-5 py-3">
                        <div className="space-y-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <span className="font-medium">{item.providerName}</span>
                            {item.providerType && (
                              <span className="rounded-full bg-muted px-2 py-0.5 text-xs capitalize text-muted-foreground">{item.providerType}</span>
                            )}
                          </div>
                          <div className="text-xs text-muted-foreground">{item.providerCode}</div>
                          <div className="text-xs text-muted-foreground">
                            {typeof item.rateLimitQpm === "number" && item.rateLimitQpm > 0 ? `${item.rateLimitQpm} QPM` : "不限"}
                          </div>
                        </div>
                      </td>
                      <td className="px-5 py-3 text-xs text-muted-foreground">
                        {item.baseUrl ? (
                          <div className="flex items-center gap-2">
                            <a href={item.baseUrl} className="truncate text-primary" target="_blank" rel="noreferrer">
                              {item.baseUrl}
                            </a>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="ml-auto h-7 w-7 text-muted-foreground"
                              onClick={async () => {
                                try {
                                  await navigator.clipboard.writeText(item.baseUrl!);
                                  toast.success("Base URL 已复制");
                                } catch {
                                  toast.error("复制失败，请稍后重试");
                                }
                              }}
                            >
                              <LuCopy className="size-4" />
                            </Button>
                          </div>
                        ) : (
                          "-"
                        )}
                      </td>
                      <td className="px-5 py-3 text-center whitespace-nowrap">
                        <Switch
                          checked={item.status === 1}
                          onCheckedChange={async (checked) => {
                            try {
                              await updateProviderStatus(item.id, checked ? 1 : 0);
                              toast.success(`已${checked ? "启用" : "停用"} ${item.providerName}`);
                              mutate();
                            } catch (err) {
                              toast.error(err instanceof Error ? err.message : "更新状态失败");
                            }
                          }}
                        />
                      </td>
                      <td className="px-5 py-3 text-center whitespace-nowrap">
                        <div className="flex justify-center gap-1.5">
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <Button variant="ghost" size="icon" aria-label="编辑供应商" onClick={() => setEditor({ mode: "edit", provider: item })}>
                                <LuPenLine className="size-4" />
                              </Button>
                            </TooltipTrigger>
                            <TooltipContent>编辑</TooltipContent>
                          </Tooltip>
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="text-destructive"
                                aria-label="删除供应商"
                                onClick={() => setPendingDelete(item)}
                              >
                                <LuTrash2 className="size-4" />
                              </Button>
                            </TooltipTrigger>
                            <TooltipContent>删除</TooltipContent>
                          </Tooltip>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
                </table>
              </TooltipProvider>
            </ScrollArea>
          )}
          <div className="flex items-center justify-between border-t px-5 py-3 text-xs text-muted-foreground">
            <div>
              第 {pagination.current}/{totalPages} 页 · 共 {total} 条
            </div>
            <div className="flex items-center gap-2">
              <Button variant="outline" size="sm" disabled={pagination.current <= 1} onClick={() => handlePaginate("prev")}>上一页</Button>
              <Button variant="outline" size="sm" disabled={pagination.current >= totalPages} onClick={() => handlePaginate("next")}>下一页</Button>
            </div>
          </div>
        </div>
      </div>

      {editor && (
        <ProviderEditor
          state={editor}
          onOpenChange={(open) => !open && setEditor(null)}
          onSubmit={async (values) => {
            try {
              const payload = toPayload(values);
              if (!payload.providerCode || !payload.providerName) {
                toast.error("请填写编码和名称");
                return;
              }
              if (editor.mode === "create") {
                await createProvider(payload);
                toast.success("已创建模型供应商");
              } else {
                await updateProvider(editor.provider.id, payload);
                toast.success("已更新模型供应商");
              }
              setEditor(null);
              mutate();
            } catch (error) {
              toast.error(error instanceof Error ? error.message : "保存失败");
            }
          }}
        />
      )}

      <AlertDialog open={!!pendingDelete} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>删除模型供应商</AlertDialogTitle>
            <AlertDialogDescription>
              确认删除供应商“{pendingDelete?.providerName}”吗？该操作不可恢复。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={async () => {
                if (!pendingDelete) return;
                try {
                  await deleteProvider(pendingDelete.id);
                  toast.success("已删除供应商");
                  setPendingDelete(null);
                  mutate();
                } catch (error) {
                  toast.error(error instanceof Error ? error.message : "删除失败");
                }
              }}
            >
              确认删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

function ProviderEditor({
  state,
  onOpenChange,
  onSubmit,
}: {
  state: EditorState;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: ProviderFormState) => Promise<void>;
}) {
  const [form, setForm] = useState<ProviderFormState>(() => toFormState(state.mode === "edit" ? state.provider : undefined));
  const [saving, setSaving] = useState(false);

  const handleChange = (key: keyof ProviderFormState, value: string | boolean) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async () => {
    setSaving(true);
    try {
      await onSubmit(form);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto p-0">
        <div className="space-y-5 px-5 py-6 sm:px-8">
          <DialogHeader className="space-y-1 text-left">
            <DialogTitle>{state.mode === "create" ? "新建模型供应商" : `编辑：${state.provider.providerName}`}</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4">
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="供应商编码" required>
                <Input value={form.providerCode} maxLength={50} onChange={(e) => handleChange("providerCode", e.target.value)} />
              </Field>
              <Field label="供应商名称" required>
                <Input value={form.providerName} maxLength={100} onChange={(e) => handleChange("providerName", e.target.value)} />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="供应商类型">
                <Select value={form.providerType} onValueChange={(value) => handleChange("providerType", value)}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择类型" />
                  </SelectTrigger>
                  <SelectContent>
                    {PROVIDER_TYPES.filter((t) => t.value !== "all").map((item) => (
                      <SelectItem key={item.value} value={item.value}>
                        {item.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
              <Field label="基础 URL">
                <Input value={form.baseUrl} onChange={(e) => handleChange("baseUrl", e.target.value)} placeholder="https://api.openai.com" />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="API Key 密文">
                <Input value={form.apiKeyCipher} onChange={(e) => handleChange("apiKeyCipher", e.target.value)} placeholder="密文或密钥别名" />
              </Field>
              <Field label="API Key 索引">
                <Input value={form.apiKeyIndex} onChange={(e) => handleChange("apiKeyIndex", e.target.value)} placeholder="KMS alias 或 HMAC" />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="限流（QPM）">
                <Input type="number" inputMode="numeric" value={form.rateLimitQpm} onChange={(e) => handleChange("rateLimitQpm", e.target.value)} />
              </Field>
              <Field label="状态">
                <div className="flex items-center gap-3 rounded-lg border px-3 py-2">
                  <Switch checked={form.status} onCheckedChange={(checked) => handleChange("status", checked)} />
                  <span className="text-sm text-muted-foreground">{form.status ? "启用" : "暂停"}</span>
                </div>
              </Field>
            </div>
            <Field label="备注">
              <Textarea value={form.remark} onChange={(e) => handleChange("remark", e.target.value)} rows={3} />
            </Field>
          </div>
        </div>
        <div className="flex justify-end gap-3 border-t px-5 py-4 sm:px-8">
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button onClick={handleSubmit} disabled={saving}>
            {saving ? "保存中..." : "保存"}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function Field({ label, required, children }: { label: string; required?: boolean; children: ReactNode }) {
  return (
    <div className="space-y-2">
      <Label className="text-xs text-muted-foreground">
        {label}
        {required && <span className="ml-1 text-destructive">*</span>}
      </Label>
      {children}
    </div>
  );
}
