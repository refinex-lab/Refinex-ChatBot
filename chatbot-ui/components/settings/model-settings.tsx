"use client";

import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuPenLine, LuPlus, LuRefreshCcw, LuTrash2} from "react-icons/lu";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectLabel, SelectTrigger, SelectValue} from "@/components/ui/select";
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
import {ScrollArea, ScrollBar} from "@/components/ui/scroll-area";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {
    AiModel,
    createModel,
    deleteModel,
    ModelMutationPayload,
    queryModels,
    updateModel,
    updateModelStatus,
} from "@/lib/api/ai/model";
import {AiProvider, queryProviders} from "@/lib/api/ai/provider";

const MODEL_TYPES = [
  { value: "all", label: "全部类型" },
  { value: "CHAT", label: "对话" },
  { value: "EMBEDDING", label: "向量" },
  { value: "IMAGE", label: "图像" },
  { value: "AUDIO", label: "音频" },
  { value: "RERANK", label: "重排" },
  { value: "MCP", label: "MCP" },
];

const API_VARIANTS = [
  { value: "all", label: "全部形态" },
  { value: "openai", label: "OpenAI" },
  { value: "azure", label: "Azure" },
  { value: "vertex", label: "Vertex" },
  { value: "bedrock", label: "Bedrock" },
  { value: "ollama", label: "Ollama" },
];
const OPTIONAL_VALUE = "__optional";

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; model: AiModel };

type ModelFormState = {
  providerId: string;
  modelKey: string;
  modelName: string;
  modelType: string;
  apiVariant: string;
  region: string;
  contextWindowTokens: string;
  maxOutputTokens: string;
  priceInputPer1k: string;
  priceOutputPer1k: string;
  currency: string;
  supportToolCall: boolean;
  supportVision: boolean;
  supportAudioIn: boolean;
  supportAudioOut: boolean;
  supportStructuredOut: boolean;
  status: boolean;
  remark: string;
};

function toFormState(model?: AiModel): ModelFormState {
  if (!model) {
    return {
      providerId: "",
      modelKey: "",
      modelName: "",
      modelType: "CHAT",
      apiVariant: "openai",
      region: "",
      contextWindowTokens: "",
      maxOutputTokens: "",
      priceInputPer1k: "",
      priceOutputPer1k: "",
      currency: "USD",
      supportToolCall: false,
      supportVision: false,
      supportAudioIn: false,
      supportAudioOut: false,
      supportStructuredOut: false,
      status: true,
      remark: "",
    };
  }
  return {
    providerId: String(model.providerId ?? ""),
    modelKey: model.modelKey ?? "",
    modelName: model.modelName ?? "",
    modelType: model.modelType ?? "CHAT",
    apiVariant: model.apiVariant ?? "",
    region: model.region ?? "",
    contextWindowTokens: model.contextWindowTokens?.toString() ?? "",
    maxOutputTokens: model.maxOutputTokens?.toString() ?? "",
    priceInputPer1k: model.priceInputPer1k?.toString() ?? "",
    priceOutputPer1k: model.priceOutputPer1k?.toString() ?? "",
    currency: model.currency ?? "USD",
    supportToolCall: (model.supportToolCall ?? 0) === 1,
    supportVision: (model.supportVision ?? 0) === 1,
    supportAudioIn: (model.supportAudioIn ?? 0) === 1,
    supportAudioOut: (model.supportAudioOut ?? 0) === 1,
    supportStructuredOut: (model.supportStructuredOut ?? 0) === 1,
    status: (model.status ?? 1) === 1,
    remark: model.remark ?? "",
  };
}

function toPayload(form: ModelFormState): ModelMutationPayload {
  return {
    providerId: Number(form.providerId),
    modelKey: form.modelKey.trim(),
    modelName: form.modelName.trim() || undefined,
    modelType: form.modelType,
    apiVariant: form.apiVariant || undefined,
    region: form.region.trim() || undefined,
    contextWindowTokens: form.contextWindowTokens ? Number(form.contextWindowTokens) : undefined,
    maxOutputTokens: form.maxOutputTokens ? Number(form.maxOutputTokens) : undefined,
    priceInputPer1k: form.priceInputPer1k ? Number(form.priceInputPer1k) : undefined,
    priceOutputPer1k: form.priceOutputPer1k ? Number(form.priceOutputPer1k) : undefined,
    currency: form.currency || undefined,
    supportToolCall: form.supportToolCall ? 1 : 0,
    supportVision: form.supportVision ? 1 : 0,
    supportAudioIn: form.supportAudioIn ? 1 : 0,
    supportAudioOut: form.supportAudioOut ? 1 : 0,
    supportStructuredOut: form.supportStructuredOut ? 1 : 0,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
}

export function ModelSettings() {
  const [filters, setFilters] = useState({ keyword: "", providerId: "all", modelType: "all", apiVariant: "all", status: "all" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiModel | null>(null);
  const [statusPendingId, setStatusPendingId] = useState<number | null>(null);

  const providerList = useSWR("model-provider-options", () =>
    queryProviders({ current: 1, size: 200 }).then((res) => res.records)
  );
  const providerOptions = providerList.data ?? [];
  const providerMap = useMemo(() => new Map(providerOptions.map((item) => [item.id, item])), [providerOptions]);

  const swrKey = useMemo(() => ["ai-models", filters, pagination] as const, [filters, pagination]);
  const { data, isLoading, error, mutate, isValidating } = useSWR(swrKey, () =>
    queryModels({
      providerId: filters.providerId === "all" ? undefined : Number(filters.providerId),
      modelType: filters.modelType === "all" ? undefined : filters.modelType,
      apiVariant: filters.apiVariant === "all" ? undefined : filters.apiVariant,
      status: filters.status === "all" ? undefined : Number(filters.status),
      keyword: filters.keyword || undefined,
      current: pagination.current,
      size: pagination.size,
    })
  );

  const models = data?.records ?? [];
  const total = data?.total ?? 0;
  const totalPages = data?.pages ?? 1;

  const handleFilter = (key: keyof typeof filters, value: string) => {
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
          {/* <LuCircuitBoard className="size-5 text-primary" /> */}
          模型管理
        </div>
      </div>

      <div className="flex-1 space-y-6 overflow-y-auto px-6 py-6">
        <div className="space-y-3 rounded-2xl border bg-background/60 p-4 shadow-sm">
          <div className="flex flex-wrap items-center gap-3">
            <Input
              className="min-w-[200px] flex-1"
              placeholder="搜索模型标识或名称"
              value={filters.keyword}
              onChange={(e) => handleFilter("keyword", e.target.value)}
            />
            <Select value={filters.providerId} onValueChange={(value) => handleFilter("providerId", value)}>
              <SelectTrigger className="w-44">
                <SelectValue placeholder="供应商" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部供应商</SelectItem>
                {providerOptions.map((provider) => (
                  <SelectItem key={provider.id} value={String(provider.id)}>
                    {provider.providerName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={filters.modelType} onValueChange={(value) => handleFilter("modelType", value)}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="模型类型" />
              </SelectTrigger>
              <SelectContent>
                {MODEL_TYPES.map((item) => (
                  <SelectItem key={item.value} value={item.value}>
                    {item.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={filters.apiVariant} onValueChange={(value) => handleFilter("apiVariant", value)}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="API 形态" />
              </SelectTrigger>
              <SelectContent>
                {API_VARIANTS.map((item) => (
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
            <Button variant="outline" size="sm" className="gap-1" onClick={() => mutate()}>
              <LuRefreshCcw className={`size-4 ${isValidating ? "animate-spin" : ""}`} /> 刷新
            </Button>
            <Button size="sm" className="gap-1" onClick={() => setEditor({ mode: "create" })}>
              <LuPlus className="size-4" /> 新建模型
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">建议为不同业务线拆分模型配置，便于控制成本与 Token 配额。</p>
        </div>

        <div className="rounded-2xl border bg-card/60 shadow-sm">
          {error && (
            <div className="px-6 py-10 text-center text-sm text-destructive">{error instanceof Error ? error.message : "加载失败"}</div>
          )}
          {!error && (
            <ScrollArea className="h-[420px]">
              <TooltipProvider delayDuration={200}>
                <div className="min-w-[980px]">
                  <table className="w-full border-collapse text-sm">
                    <thead>
                      <tr className="sticky top-0 bg-muted/60 text-left text-xs uppercase text-muted-foreground">
                        <th className="sticky left-0 top-0 z-30 w-[300px] bg-muted/80 px-5 py-3 text-left font-medium">模型</th>
                        <th className="w-[32%] px-5 py-3 font-medium">API / Region</th>
                        <th className="w-[26%] px-5 py-3 font-medium">能力</th>
                        <th className="w-[12%] px-5 py-3 text-center font-medium">状态</th>
                        <th className="sticky right-0 top-0 z-30 w-[120px] bg-muted/80 px-5 py-3 text-center font-medium">操作</th>
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
                  {models.length === 0 && !isLoading && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无模型记录
                      </td>
                    </tr>
                  )}
                  {models.map((model) => {
                    const provider = providerMap.get(model.providerId);
                    return (
                      <tr key={model.id} className="border-t border-border/60">
                        <td className="sticky left-0 z-20 w-[300px] bg-card/90 px-5 py-3 text-left backdrop-blur supports-[backdrop-filter]:bg-card/75">
                          <div className="space-y-1">
                            <div className="flex items-center gap-2">
                              <span className="font-medium truncate">{model.modelName || model.modelKey}</span>
                              <span className="rounded-full bg-muted px-2 py-0.5 text-[11px] text-muted-foreground">{model.modelType}</span>
                            </div>
                            <div className="text-xs text-muted-foreground truncate">{model.modelKey}</div>
                            <div className="text-xs text-muted-foreground truncate">{provider?.providerName ?? `#${model.providerId}`}</div>
                            {(model.priceInputPer1k || model.priceOutputPer1k) && (
                              <div className="text-xs text-muted-foreground">
                                成本: {model.priceInputPer1k ? `In $${model.priceInputPer1k}/1K` : "-"}
                                {model.priceOutputPer1k ? ` · Out $${model.priceOutputPer1k}/1K` : ""}
                              </div>
                            )}
                          </div>
                        </td>
                        <td className="px-5 py-3 text-xs text-muted-foreground">
                          <div className="font-medium">{model.apiVariant || "-"}</div>
                          {model.region && <div className="text-[11px] text-muted-foreground/80">Region: {model.region}</div>}
                          {model.contextWindowTokens || model.maxOutputTokens ? (
                            <div className="mt-1 text-[11px] text-muted-foreground">
                              {model.contextWindowTokens ? `${model.contextWindowTokens} ctx` : "-"}
                              {model.maxOutputTokens ? ` · ${model.maxOutputTokens} out` : ""}
                            </div>
                          ) : null}
                        </td>
                        <td className="px-5 py-3">
                          <div className="flex flex-wrap gap-1 text-[11px]">
                            {model.supportToolCall ? <CapabilityBadge>Tool</CapabilityBadge> : null}
                            {model.supportVision ? <CapabilityBadge>Vision</CapabilityBadge> : null}
                            {model.supportAudioIn ? <CapabilityBadge>Audio In</CapabilityBadge> : null}
                            {model.supportAudioOut ? <CapabilityBadge>Audio Out</CapabilityBadge> : null}
                            {model.supportStructuredOut ? <CapabilityBadge>Struct</CapabilityBadge> : null}
                          </div>
                        </td>
                        <td className="px-5 py-3 text-center whitespace-nowrap">
                          <div className="flex items-center justify-center gap-2">
                            <Switch
                              checked={model.status === 1}
                              disabled={statusPendingId === model.id}
                              onCheckedChange={async (checked) => {
                                setStatusPendingId(model.id);
                                try {
                                  await updateModelStatus(model.id, checked ? 1 : 0);
                                  toast.success(`已${checked ? "启用" : "停用"} ${model.modelName || model.modelKey}`);
                                  mutate();
                                } catch (err) {
                                  toast.error(err instanceof Error ? err.message : "更新状态失败");
                                } finally {
                                  setStatusPendingId(null);
                                }
                              }}
                            />
                            <span className="text-xs text-muted-foreground">{model.status === 1 ? "启用" : "停用"}</span>
                          </div>
                        </td>
                        <td className="sticky right-0 z-20 w-[120px] bg-card/95 px-5 py-3 text-center backdrop-blur supports-[backdrop-filter]:bg-card/80">
                          <div className="flex justify-center gap-1.5">
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button variant="ghost" size="icon" aria-label="编辑模型" onClick={() => setEditor({ mode: "edit", model })}>
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
                                  aria-label="删除模型"
                                  onClick={() => setPendingDelete(model)}
                                >
                                  <LuTrash2 className="size-4" />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent>删除</TooltipContent>
                            </Tooltip>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                    </tbody>
                  </table>
                </div>
                <ScrollBar orientation="horizontal" />
              </TooltipProvider>
            </ScrollArea>
          )}
          <div className="flex items-center justify-between border-t px-5 py-3 text-xs text-muted-foreground">
            <div>
              第 {pagination.current}/{totalPages} 页 · 共 {total} 条
            </div>
            <div className="flex items-center gap-2">
              <Button variant="outline" size="sm" disabled={pagination.current <= 1} onClick={() => handlePaginate("prev")}>
                上一页
              </Button>
              <Button variant="outline" size="sm" disabled={pagination.current >= totalPages} onClick={() => handlePaginate("next")}>
                下一页
              </Button>
            </div>
          </div>
        </div>
      </div>

      {editor && (
        <ModelEditor
          state={editor}
          providers={providerOptions}
          onOpenChange={(open) => !open && setEditor(null)}
          onSubmit={async (values) => {
            try {
              const payload = toPayload(values);
              if (!payload.providerId || !payload.modelKey) {
                toast.error("请填写供应商与模型标识");
                return;
              }
              if (editor.mode === "create") {
                await createModel(payload);
                toast.success("已创建模型");
              } else {
                await updateModel(editor.model.id, payload);
                toast.success("已更新模型");
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
            <AlertDialogTitle>删除模型</AlertDialogTitle>
            <AlertDialogDescription>
              确认删除模型“{pendingDelete?.modelName || pendingDelete?.modelKey}”吗？该操作不可恢复。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={async () => {
                if (!pendingDelete) return;
                try {
                  await deleteModel(pendingDelete.id);
                  toast.success("已删除模型");
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

function ModelEditor({
  state,
  providers,
  onOpenChange,
  onSubmit,
}: {
  state: EditorState;
  providers: AiProvider[];
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: ModelFormState) => Promise<void>;
}) {
  const [form, setForm] = useState<ModelFormState>(() => toFormState(state.mode === "edit" ? state.model : undefined));
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!form.providerId && providers.length > 0) {
      setForm((prev) => ({ ...prev, providerId: String(providers[0].id) }));
    }
  }, [providers, form.providerId]);

  const handleChange = (key: keyof ModelFormState, value: string | boolean) => {
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
      <DialogContent className="max-h-[92vh] max-w-5xl overflow-y-auto p-0">
        <div className="space-y-5 px-5 py-6 sm:px-8">
          <DialogHeader className="space-y-1 text-left">
            <DialogTitle>{state.mode === "create" ? "新建模型" : `编辑：${state.model.modelName || state.model.modelKey}`}</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4">
            <div className="grid gap-4 md:grid-cols-2">
            <Field label="模型供应商" required>
              <Select
                value={form.providerId}
                onValueChange={(value) => handleChange("providerId", value)}
                disabled={providers.length === 0}
              >
                <SelectTrigger>
                  <SelectValue placeholder="选择供应商" />
                </SelectTrigger>
                <SelectContent>
                  {providers.length === 0 ? (
                    <SelectLabel className="text-xs text-muted-foreground">暂无供应商，请先创建</SelectLabel>
                  ) : (
                    providers.map((provider) => (
                      <SelectItem key={provider.id} value={String(provider.id)}>
                        {provider.providerName}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </Field>
            <Field label="模型标识" required>
              <Input value={form.modelKey} maxLength={100} onChange={(e) => handleChange("modelKey", e.target.value)} placeholder="gpt-4o-mini" />
            </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="模型名称">
                <Input value={form.modelName} onChange={(e) => handleChange("modelName", e.target.value)} placeholder="4o Mini" />
              </Field>
              <Field label="模型类型">
                <Select value={form.modelType} onValueChange={(value) => handleChange("modelType", value)}>
                  <SelectTrigger>
                    <SelectValue placeholder="类型" />
                  </SelectTrigger>
                  <SelectContent>
                    {MODEL_TYPES.filter((t) => t.value !== "all").map((item) => (
                      <SelectItem key={item.value} value={item.value}>
                        {item.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="API 形态">
                <Select
                  value={form.apiVariant || OPTIONAL_VALUE}
                  onValueChange={(value) => handleChange("apiVariant", value === OPTIONAL_VALUE ? "" : value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="API" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={OPTIONAL_VALUE}>未指定</SelectItem>
                    {API_VARIANTS.filter((t) => t.value !== "all").map((item) => (
                      <SelectItem key={item.value} value={item.value}>
                        {item.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
              <Field label="区域">
                <Input value={form.region} onChange={(e) => handleChange("region", e.target.value)} placeholder="eastasia" />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-3">
              <Field label="上下文窗口（tokens）">
                <Input type="number" inputMode="numeric" value={form.contextWindowTokens} onChange={(e) => handleChange("contextWindowTokens", e.target.value)} />
              </Field>
            <Field label="最大输出（tokens）">
              <Input type="number" inputMode="numeric" value={form.maxOutputTokens} onChange={(e) => handleChange("maxOutputTokens", e.target.value)} />
            </Field>
              <Field label="币种">
                <Input value={form.currency} maxLength={10} onChange={(e) => handleChange("currency", e.target.value.toUpperCase())} />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="输入价格 / 1K tokens (USD)">
                <Input type="number" inputMode="decimal" value={form.priceInputPer1k} onChange={(e) => handleChange("priceInputPer1k", e.target.value)} />
              </Field>
              <Field label="输出价格 / 1K tokens (USD)">
                <Input type="number" inputMode="decimal" value={form.priceOutputPer1k} onChange={(e) => handleChange("priceOutputPer1k", e.target.value)} />
              </Field>
            </div>
            <Field label="能力支持">
              <div className="grid grid-cols-2 gap-3 rounded-2xl border px-4 py-3 text-sm">
                <ToggleRow label="工具调用" checked={form.supportToolCall} onChange={(val) => handleChange("supportToolCall", val)} />
                <ToggleRow label="图像多模态" checked={form.supportVision} onChange={(val) => handleChange("supportVision", val)} />
                <ToggleRow label="音频输入" checked={form.supportAudioIn} onChange={(val) => handleChange("supportAudioIn", val)} />
                <ToggleRow label="音频输出" checked={form.supportAudioOut} onChange={(val) => handleChange("supportAudioOut", val)} />
                <ToggleRow label="结构化输出" checked={form.supportStructuredOut} onChange={(val) => handleChange("supportStructuredOut", val)} />
                <ToggleRow label="状态启用" checked={form.status} onChange={(val) => handleChange("status", val)} />
              </div>
            </Field>
            <Field label="备注">
              <Textarea value={form.remark} rows={3} onChange={(e) => handleChange("remark", e.target.value)} />
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

function ToggleRow({ label, checked, onChange }: { label: string; checked: boolean; onChange: (checked: boolean) => void }) {
  return (
    <div className="flex items-center justify-between rounded-lg bg-muted/40 px-3 py-2">
      <span>{label}</span>
      <Switch checked={checked} onCheckedChange={onChange} />
    </div>
  );
}

function CapabilityBadge({ children }: { children: ReactNode }) {
  return <span className="rounded-full bg-muted px-2 py-0.5 text-[11px] text-muted-foreground">{children}</span>;
}
