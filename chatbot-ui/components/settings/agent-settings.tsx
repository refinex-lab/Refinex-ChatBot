"use client";

import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuBot, LuPenLine, LuPlus, LuRefreshCcw, LuTrash2} from "react-icons/lu";
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
import {ScrollArea} from "@/components/ui/scroll-area";
import {
    AgentMutationPayload,
    AgentQuery,
    AiAgent,
    createAgent,
    deleteAgent,
    queryAgents,
    updateAgent,
    updateAgentStatus,
} from "@/lib/api/ai/agent";
import {AiModel, queryModels} from "@/lib/api/ai/model";
import {AiPrompt, queryPrompts} from "@/lib/api/ai/prompts";
import {AiSchema, querySchemas} from "@/lib/api/ai/schema";
import {AiTool, queryTools} from "@/lib/api/ai/tool";
import {AiAdvisor, queryAdvisors} from "@/lib/api/ai/advisor";

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

const OPTIONAL_SELECT_VALUE = "__none__";

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; agent: AiAgent };

type AgentFormState = {
  agentCode: string;
  agentName: string;
  description: string;
  modelId: string;
  promptId: string;
  outputSchemaId: string;
  ragKbId: string;
  temperature: string;
  topP: string;
  presencePenalty: string;
  frequencyPenalty: string;
  maxTokens: string;
  stopSequences: string;
  toolChoice: string;
  status: boolean;
  remark: string;
  toolIds: number[];
  advisorIds: number[];
};

function toFormState(agent?: AiAgent): AgentFormState {
  if (!agent) {
    return {
      agentCode: "",
      agentName: "",
      description: "",
      modelId: "",
      promptId: "",
      outputSchemaId: "",
      ragKbId: "",
      temperature: "",
      topP: "",
      presencePenalty: "",
      frequencyPenalty: "",
      maxTokens: "",
      stopSequences: "",
      toolChoice: "",
      status: true,
      remark: "",
      toolIds: [],
      advisorIds: [],
    };
  }
  return {
    agentCode: agent.agentCode ?? "",
    agentName: agent.agentName ?? "",
    description: agent.description ?? "",
    modelId: agent.modelId ? String(agent.modelId) : "",
    promptId: agent.promptId ? String(agent.promptId) : "",
    outputSchemaId: agent.outputSchemaId ? String(agent.outputSchemaId) : "",
    ragKbId: agent.ragKbId ? String(agent.ragKbId) : "",
    temperature: agent.temperature?.toString() ?? "",
    topP: agent.topP?.toString() ?? "",
    presencePenalty: agent.presencePenalty?.toString() ?? "",
    frequencyPenalty: agent.frequencyPenalty?.toString() ?? "",
    maxTokens: agent.maxTokens?.toString() ?? "",
    stopSequences: agent.stopSequences?.join("\n") ?? "",
    toolChoice: agent.toolChoice ?? "",
    status: (agent.status ?? 1) === 1,
    remark: agent.remark ?? "",
    toolIds: agent.toolIds ?? [],
    advisorIds: agent.advisorIds ?? [],
  };
}

function buildPayload(form: AgentFormState): AgentMutationPayload {
  if (!form.modelId) {
    throw new Error("请选择默认模型");
  }
  const payload: AgentMutationPayload = {
    agentCode: form.agentCode.trim(),
    agentName: form.agentName.trim(),
    description: form.description.trim() || undefined,
    modelId: Number(form.modelId),
    promptId: form.promptId ? Number(form.promptId) : undefined,
    outputSchemaId: form.outputSchemaId ? Number(form.outputSchemaId) : undefined,
    ragKbId: form.ragKbId ? Number(form.ragKbId) : undefined,
    temperature: form.temperature ? Number(form.temperature) : undefined,
    topP: form.topP ? Number(form.topP) : undefined,
    presencePenalty: form.presencePenalty ? Number(form.presencePenalty) : undefined,
    frequencyPenalty: form.frequencyPenalty ? Number(form.frequencyPenalty) : undefined,
    maxTokens: form.maxTokens ? Number(form.maxTokens) : undefined,
    stopSequences: parseStopSequences(form.stopSequences),
    toolChoice: form.toolChoice.trim() || undefined,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
    toolIds: form.toolIds,
    advisorIds: form.advisorIds,
  };
  return payload;
}

function parseStopSequences(value: string): string[] | undefined {
  const tokens = value
    .split(/[\n,]/)
    .map((item) => item.trim())
    .filter(Boolean);
  return tokens.length > 0 ? tokens : undefined;
}

export function AgentSettings() {
  const [filters, setFilters] = useState({ keyword: "", modelId: "all", status: "all" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiAgent | null>(null);
  const [statusPendingId, setStatusPendingId] = useState<number | null>(null);

  const queryParams: AgentQuery = useMemo(
    () => ({
      keyword: filters.keyword || undefined,
      modelId: filters.modelId === "all" ? undefined : Number(filters.modelId),
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    }),
    [filters, pagination]
  );

  const swrKey = useMemo(() => ["ai-agents", queryParams] as const, [queryParams]);
  const { data, error, isLoading, mutate, isValidating } = useSWR(swrKey, () => queryAgents(queryParams));

  const modelOptions = useSWR("ai-agent-model-options", () =>
    queryModels({ modelType: "CHAT", status: 1, current: 1, size: 200 }).then((res) => res.records)
  );
  const promptOptions = useSWR("ai-agent-prompt-options", () =>
    queryPrompts({ status: 1, current: 1, size: 200 }).then((res) => res.records)
  );
  const schemaOptions = useSWR("ai-agent-schema-options", () =>
    querySchemas({ status: 1, current: 1, size: 200 }).then((res) => res.records)
  );
  const toolOptions = useSWR("ai-agent-tool-options", () =>
    queryTools({ status: 1, current: 1, size: 200 }).then((res) => res.records)
  );
  const advisorOptions = useSWR("ai-agent-advisor-options", () =>
    queryAdvisors({ status: 1, current: 1, size: 200 }).then((res) => res.records)
  );

  const agents = data?.records ?? [];
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

  const handleSubmit = async (form: AgentFormState) => {
    let payload: AgentMutationPayload;
    try {
      payload = buildPayload(form);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "表单数据异常");
      return;
    }
    if (!payload.agentCode || !payload.agentName) {
      toast.error("请填写 Agent 编码和名称");
      return;
    }
    try {
      if (editor?.mode === "edit") {
        await updateAgent(editor.agent.id, payload);
        toast.success(`已更新 ${editor.agent.agentName}`);
      } else {
        await createAgent(payload);
        toast.success("已创建 Agent");
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
      await deleteAgent(pendingDelete.id);
      toast.success(`已删除 ${pendingDelete.agentName}`);
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
          <LuBot className="size-5 text-primary" />
          Agent 管理
        </div>
        <p className="text-xs text-muted-foreground">将模型、提示词、工具、Advisor 等能力封装成可配置的 Agent。</p>
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
            <Select value={filters.modelId} onValueChange={(value) => handleFilter("modelId", value)}>
              <SelectTrigger className="w-52">
                <SelectValue placeholder="默认模型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部模型</SelectItem>
                {modelOptions.data?.map((model) => (
                  <SelectItem key={model.id} value={String(model.id)}>
                    {model.modelName || model.modelKey}
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
              <LuRefreshCcw className={`size-4 ${isValidating ? "animate-spin" : ""}`} />
              刷新
            </Button>
            <Button size="sm" className="gap-1" onClick={() => setEditor({ mode: "create" })}>
              <LuPlus className="size-4" />
              新建 Agent
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">Agent 与工具/Advisor 绑定后，可直接在对话、任务流程中复用，支撑多业务协作。</p>
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
                    <th className="w-[30%] px-5 py-3 font-medium">Agent</th>
                    <th className="w-[25%] px-5 py-3 font-medium">默认模型 / Prompt</th>
                    <th className="w-[15%] px-5 py-3 text-center font-medium">工具/Advisor</th>
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
                  {!isLoading && agents.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无 Agent
                      </td>
                    </tr>
                  )}
                  {agents.map((agent) => (
                    <tr key={agent.id} className="border-t border-border/60">
                      <td className="px-5 py-3">
                        <div className="space-y-1">
                          <div className="font-medium">{agent.agentName}</div>
                          <div className="text-xs text-muted-foreground">{agent.agentCode}</div>
                          {agent.remark && <p className="text-xs text-muted-foreground line-clamp-2">{agent.remark}</p>}
                        </div>
                      </td>
                      <td className="px-5 py-3 text-xs text-muted-foreground">
                        <p>
                          模型：
                          {modelOptions.data?.find((model) => model.id === agent.modelId)?.modelName ||
                            modelOptions.data?.find((model) => model.id === agent.modelId)?.modelKey ||
                            `#${agent.modelId}`}
                        </p>
                        <p>
                          Prompt：{promptOptions.data?.find((prompt) => prompt.id === agent.promptId)?.promptName || "-"}
                        </p>
                        <p>Schema：{schemaOptions.data?.find((schema) => schema.id === agent.outputSchemaId)?.schemaName || "-"}</p>
                      </td>
                      <td className="px-5 py-3 text-center text-xs text-muted-foreground">
                        <p>工具 {agent.toolIds?.length ?? 0} 个</p>
                        <p>Advisor {agent.advisorIds?.length ?? 0} 个</p>
                      </td>
                      <td className="px-5 py-3 text-center">
                        <Switch
                          checked={agent.status === 1}
                          disabled={statusPendingId === agent.id}
                          onCheckedChange={async (checked) => {
                            setStatusPendingId(agent.id);
                            try {
                              await updateAgentStatus(agent.id, checked ? 1 : 0);
                              toast.success(`已${checked ? "启用" : "停用"} ${agent.agentName}`);
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
                          <Button variant="ghost" size="icon" onClick={() => setEditor({ mode: "edit", agent })}>
                            <LuPenLine className="size-4" />
                          </Button>
                          <Button variant="ghost" size="icon" onClick={() => setPendingDelete(agent)}>
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

      <AgentEditorDialog
        editor={editor}
        onClose={() => setEditor(null)}
        onSubmit={handleSubmit}
        models={modelOptions.data ?? []}
        prompts={promptOptions.data ?? []}
        schemas={schemaOptions.data ?? []}
        tools={toolOptions.data ?? []}
        advisors={advisorOptions.data ?? []}
      />

      <AlertDialog open={Boolean(pendingDelete)} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除 Agent ？</AlertDialogTitle>
            <AlertDialogDescription>
              删除后 Agent 将无法在对话中使用，是否确认删除 <span className="font-semibold">{pendingDelete?.agentName}</span>？
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

type OptionItem = { value: number; label: string; description?: string | null };

function AgentEditorDialog({
  editor,
  onClose,
  onSubmit,
  models,
  prompts,
  schemas,
  tools,
  advisors,
}: {
  editor: EditorState | null;
  onClose: () => void;
  onSubmit: (form: AgentFormState) => void;
  models: AiModel[];
  prompts: AiPrompt[];
  schemas: AiSchema[];
  tools: AiTool[];
  advisors: AiAdvisor[];
}) {
  const [form, setForm] = useState<AgentFormState>(toFormState());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm(toFormState(editor && editor.mode === "edit" ? editor.agent : undefined));
    setSaving(false);
  }, [editor]);

  const open = Boolean(editor);

  const toolOptions: OptionItem[] = tools.map((tool) => ({
    value: tool.id,
    label: tool.toolName ?? tool.toolCode,
    description: tool.toolType,
  }));
  const advisorOptions: OptionItem[] = advisors.map((advisor) => ({
    value: advisor.id,
    label: advisor.advisorName,
    description: advisor.advisorType,
  }));

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="max-h-[85vh] overflow-y-auto p-5">
        <DialogHeader>
          <DialogTitle>{editor?.mode === "edit" ? "编辑 Agent" : "新建 Agent"}</DialogTitle>
        </DialogHeader>
        <form
          className="space-y-5 pt-2"
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
            <Field label="Agent 编码 *">
              <Input value={form.agentCode} required onChange={(e) => setForm((prev) => ({ ...prev, agentCode: e.target.value }))} />
            </Field>
            <Field label="Agent 名称 *">
              <Input value={form.agentName} required onChange={(e) => setForm((prev) => ({ ...prev, agentName: e.target.value }))} />
            </Field>
            <Field label="默认模型 *">
              <Select value={form.modelId} onValueChange={(value) => setForm((prev) => ({ ...prev, modelId: value }))}>
                <SelectTrigger>
                  <SelectValue placeholder="选择模型" />
                </SelectTrigger>
                <SelectContent>
                  {models.map((model) => (
                    <SelectItem key={model.id} value={String(model.id)}>
                      {model.modelName || model.modelKey}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <Field label="默认 Prompt">
              <Select
                value={form.promptId ? form.promptId : OPTIONAL_SELECT_VALUE}
                onValueChange={(value) => setForm((prev) => ({ ...prev, promptId: value === OPTIONAL_SELECT_VALUE ? "" : value }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="可选" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={OPTIONAL_SELECT_VALUE}>未绑定</SelectItem>
                  {prompts.map((prompt) => (
                    <SelectItem key={prompt.id} value={String(prompt.id)}>
                      {prompt.promptName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <Field label="结构化输出 Schema">
              <Select
                value={form.outputSchemaId ? form.outputSchemaId : OPTIONAL_SELECT_VALUE}
                onValueChange={(value) =>
                  setForm((prev) => ({ ...prev, outputSchemaId: value === OPTIONAL_SELECT_VALUE ? "" : value }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="可选" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={OPTIONAL_SELECT_VALUE}>不约束</SelectItem>
                  {schemas.map((schema) => (
                    <SelectItem key={schema.id} value={String(schema.id)}>
                      {schema.schemaName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <Field label="默认知识库 ID">
              <Input value={form.ragKbId} onChange={(e) => setForm((prev) => ({ ...prev, ragKbId: e.target.value }))} />
            </Field>
          </div>

          <Field label="Agent 描述">
            <Textarea rows={3} value={form.description} onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))} />
          </Field>

          <div className="grid grid-cols-2 gap-4">
            <Field label="温度 (0~2)">
              <Input
                type="number"
                step="0.01"
                value={form.temperature}
                onChange={(e) => setForm((prev) => ({ ...prev, temperature: e.target.value }))}
              />
            </Field>
            <Field label="Top P">
              <Input type="number" step="0.01" value={form.topP} onChange={(e) => setForm((prev) => ({ ...prev, topP: e.target.value }))} />
            </Field>
            <Field label="Presence Penalty">
              <Input
                type="number"
                step="0.01"
                value={form.presencePenalty}
                onChange={(e) => setForm((prev) => ({ ...prev, presencePenalty: e.target.value }))}
              />
            </Field>
            <Field label="Frequency Penalty">
              <Input
                type="number"
                step="0.01"
                value={form.frequencyPenalty}
                onChange={(e) => setForm((prev) => ({ ...prev, frequencyPenalty: e.target.value }))}
              />
            </Field>
            <Field label="最大生成 Tokens">
              <Input
                type="number"
                min={0}
                value={form.maxTokens}
                onChange={(e) => setForm((prev) => ({ ...prev, maxTokens: e.target.value }))}
              />
            </Field>
            <Field label="工具调用策略">
              <Input value={form.toolChoice} onChange={(e) => setForm((prev) => ({ ...prev, toolChoice: e.target.value }))} />
            </Field>
          </div>

          <Field label="停止序列（换行/逗号分隔）">
            <Textarea
              rows={3}
              placeholder="例如：### END"
              value={form.stopSequences}
              onChange={(e) => setForm((prev) => ({ ...prev, stopSequences: e.target.value }))}
            />
          </Field>

          <div className="grid grid-cols-2 gap-4">
            <Field label="启用状态">
              <div className="flex items-center gap-2 rounded-md border px-3 py-2">
                <Switch checked={form.status} onCheckedChange={(checked) => setForm((prev) => ({ ...prev, status: checked }))} />
                <span className="text-sm text-muted-foreground">{form.status ? "启用" : "停用"}</span>
              </div>
            </Field>
          </div>

          <MultiSelectChips
            label="绑定工具"
            options={toolOptions}
            selected={form.toolIds}
            onToggle={(id) =>
              setForm((prev) => ({
                ...prev,
                toolIds: prev.toolIds.includes(id) ? prev.toolIds.filter((item) => item !== id) : [...prev.toolIds, id],
              }))
            }
          />

          <MultiSelectChips
            label="绑定 Advisor"
            options={advisorOptions}
            selected={form.advisorIds}
            onToggle={(id) =>
              setForm((prev) => ({
                ...prev,
                advisorIds: prev.advisorIds.includes(id) ? prev.advisorIds.filter((item) => item !== id) : [...prev.advisorIds, id],
              }))
            }
          />

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

function MultiSelectChips({
  label,
  options,
  selected,
  onToggle,
}: {
  label: string;
  options: OptionItem[];
  selected: number[];
  onToggle: (id: number) => void;
}) {
  return (
    <div className="space-y-2 rounded-xl border p-3">
      <div className="flex items-center justify-between text-sm">
        <span className="text-muted-foreground">{label}</span>
        <span className="text-xs text-muted-foreground">
          已选 {selected.length}/{options.length}
        </span>
      </div>
      <div className="flex flex-wrap gap-2">
        {options.map((option) => {
          const active = selected.includes(option.value);
          return (
            <button
              type="button"
              key={option.value}
              className={`rounded-full border px-3 py-1 text-xs transition ${
                active ? "border-primary bg-primary/10 text-primary" : "text-muted-foreground hover:text-foreground"
              }`}
              onClick={() => onToggle(option.value)}
            >
              <span className="font-medium">{option.label}</span>
              {option.description && <span className="ml-1 text-[11px] text-muted-foreground">{option.description}</span>}
            </button>
          );
        })}
        {options.length === 0 && <span className="text-xs text-muted-foreground">暂无可选项</span>}
      </div>
    </div>
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
