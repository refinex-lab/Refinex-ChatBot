"use client";

import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuPenLine, LuPlus, LuRefreshCcw, LuTrash2, LuWrench} from "react-icons/lu";
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
    AiTool,
    createTool,
    deleteTool,
    queryTools,
    ToolMutationPayload,
    ToolQuery,
    updateTool,
    updateToolStatus,
} from "@/lib/api/ai/tool";
import {AiMcpServer, queryMcpServers} from "@/lib/api/ai/mcp-server";

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

const MCP_UNBOUND_VALUE = "__none__";

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; tool: AiTool };

type ToolFormState = {
  toolCode: string;
  toolName: string;
  toolType: string;
  implBean: string;
  endpoint: string;
  timeoutMs: string;
  inputSchema: string;
  outputSchema: string;
  mcpServerId: string;
  status: boolean;
  remark: string;
};

function toFormState(tool?: AiTool): ToolFormState {
  if (!tool) {
    return {
      toolCode: "",
      toolName: "",
      toolType: "",
      implBean: "",
      endpoint: "",
      timeoutMs: "",
      inputSchema: "",
      outputSchema: "",
      mcpServerId: "",
      status: true,
      remark: "",
    };
  }
  return {
    toolCode: tool.toolCode ?? "",
    toolName: tool.toolName ?? "",
    toolType: tool.toolType ?? "",
    implBean: tool.implBean ?? "",
    endpoint: tool.endpoint ?? "",
    timeoutMs: tool.timeoutMs?.toString() ?? "",
    inputSchema: tool.inputSchema ? JSON.stringify(tool.inputSchema, null, 2) : "",
    outputSchema: tool.outputSchema ? JSON.stringify(tool.outputSchema, null, 2) : "",
    mcpServerId: tool.mcpServerId ? String(tool.mcpServerId) : "",
    status: (tool.status ?? 1) === 1,
    remark: tool.remark ?? "",
  };
}

function buildPayload(form: ToolFormState): ToolMutationPayload {
  const payload: ToolMutationPayload = {
    toolCode: form.toolCode.trim(),
    toolName: form.toolName.trim(),
    toolType: form.toolType.trim(),
    implBean: form.implBean.trim() || undefined,
    endpoint: form.endpoint.trim() || undefined,
    timeoutMs: form.timeoutMs ? Number(form.timeoutMs) : undefined,
    mcpServerId: form.mcpServerId ? Number(form.mcpServerId) : undefined,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
  if (form.inputSchema) {
    try {
      payload.inputSchema = JSON.parse(form.inputSchema);
    } catch {
      throw new Error("输入 Schema 不是合法 JSON");
    }
  }
  if (form.outputSchema) {
    try {
      payload.outputSchema = JSON.parse(form.outputSchema);
    } catch {
      throw new Error("输出 Schema 不是合法 JSON");
    }
  }
  return payload;
}

export function ToolSettings() {
  const [filters, setFilters] = useState({ keyword: "", status: "all", toolType: "" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiTool | null>(null);
  const [statusPendingId, setStatusPendingId] = useState<number | null>(null);

  const queryParams: ToolQuery = useMemo(
    () => ({
      keyword: filters.keyword || undefined,
      toolType: filters.toolType || undefined,
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    }),
    [filters, pagination]
  );

  const swrKey = useMemo(() => ["ai-tools", queryParams] as const, [queryParams]);
  const { data, error, isLoading, mutate, isValidating } = useSWR(swrKey, () => queryTools(queryParams));

  const mcpOptions = useSWR("ai-mcp-options", () =>
    queryMcpServers({ status: 1, current: 1, size: 200 }).then((res) => res.records)
  );

  const tools = data?.records ?? [];
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

  const handleSubmit = async (form: ToolFormState) => {
    let payload: ToolMutationPayload;
    try {
      payload = buildPayload(form);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "表单解析失败");
      return;
    }
    if (!payload.toolCode || !payload.toolName || !payload.toolType) {
      toast.error("请完善工具编码/名称/类型");
      return;
    }
    try {
      if (editor?.mode === "edit") {
        await updateTool(editor.tool.id, payload);
        toast.success(`已更新工具 ${editor.tool.toolName}`);
      } else {
        await createTool(payload);
        toast.success("已创建工具");
      }
      setEditor(null);
      mutate();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "保存工具失败");
    }
  };

  const handleDelete = async () => {
    if (!pendingDelete) return;
    try {
      await deleteTool(pendingDelete.id);
      toast.success(`已删除工具 ${pendingDelete.toolName}`);
      setPendingDelete(null);
      mutate();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "删除工具失败");
    }
  };

  return (
    <div className="flex h-full flex-col">
      <div className="border-b px-6 py-3">
        <div className="flex items-center gap-2 text-lg font-semibold">
          <LuWrench className="size-5 text-primary" />
          工具管理
        </div>
        <p className="text-xs text-muted-foreground">统一管理 Function/MCP 工具，实现 Agent 编排与复用。</p>
      </div>

      <div className="flex-1 space-y-6 overflow-y-auto px-6 py-6">
        <div className="space-y-3 rounded-2xl border bg-background/60 p-4 shadow-sm">
          <div className="flex flex-wrap items-center gap-3">
            <Input
              className="min-w-[200px] flex-1"
              placeholder="搜索编码/名称/描述…"
              value={filters.keyword}
              onChange={(e) => handleFilter("keyword", e.target.value)}
            />
            <Input
              className="w-44"
              placeholder="工具类型，如 http/function"
              value={filters.toolType}
              onChange={(e) => handleFilter("toolType", e.target.value)}
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
              <LuPlus className="size-4" /> 新建工具
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">支持为工具绑定 MCP Server，同时配置超时、输入输出 Schema。</p>
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
                  <tr className="sticky top-0 bg-muted/40 text-left text-xs uppercase text-muted-foreground">
                    <th className="w-[32%] px-5 py-3 font-medium">工具</th>
                    <th className="w-[28%] px-5 py-3 font-medium">MCP / 端点</th>
                    <th className="w-[15%] px-5 py-3 text-center font-medium">超时</th>
                    <th className="w-[10%] px-5 py-3 text-center font-medium">状态</th>
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
                  {!isLoading && tools.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无工具配置
                      </td>
                    </tr>
                  )}
                  {tools.map((tool) => (
                    <tr key={tool.id} className="border-t border-border/60">
                      <td className="px-5 py-3">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="font-medium">{tool.toolName}</span>
                            <span className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground">{tool.toolType}</span>
                          </div>
                          <p className="text-xs text-muted-foreground">{tool.toolCode}</p>
                          {tool.remark && <p className="text-xs text-muted-foreground line-clamp-2">{tool.remark}</p>}
                        </div>
                      </td>
                      <td className="px-5 py-3 text-xs text-muted-foreground">
                        <div className="space-y-1">
                          <span>
                            {tool.mcpServerId
                              ? mcpOptions.data?.find((m) => m.id === tool.mcpServerId)?.serverName ?? `MCP #${tool.mcpServerId}`
                              : "未绑定 MCP"}
                          </span>
                          <span className="block truncate text-foreground">{tool.endpoint || "-"}</span>
                        </div>
                      </td>
                      <td className="px-5 py-3 text-center text-xs text-muted-foreground">
                        {tool.timeoutMs ? `${tool.timeoutMs}ms` : "默认"}
                      </td>
                      <td className="px-5 py-3 text-center">
                        <Switch
                          disabled={statusPendingId === tool.id}
                          checked={tool.status === 1}
                          onCheckedChange={async (checked) => {
                            setStatusPendingId(tool.id);
                            try {
                              await updateToolStatus(tool.id, checked ? 1 : 0);
                              toast.success(`已${checked ? "启用" : "停用"} ${tool.toolName}`);
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
                          <Button variant="ghost" size="icon" onClick={() => setEditor({ mode: "edit", tool })}>
                            <LuPenLine className="size-4" />
                          </Button>
                          <Button variant="ghost" size="icon" onClick={() => setPendingDelete(tool)}>
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

      <ToolEditorDialog
        editor={editor}
        onClose={() => setEditor(null)}
        onSubmit={handleSubmit}
        mcpServers={mcpOptions.data ?? []}
      />

      <AlertDialog open={Boolean(pendingDelete)} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除工具？</AlertDialogTitle>
            <AlertDialogDescription>
              删除后 Agent 将无法继续引用 <span className="font-semibold">{pendingDelete?.toolName}</span>，请再次确认。
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

function ToolEditorDialog({
  editor,
  onClose,
  onSubmit,
  mcpServers,
}: {
  editor: EditorState | null;
  onClose: () => void;
  onSubmit: (form: ToolFormState) => void;
  mcpServers: AiMcpServer[];
}) {
  const [form, setForm] = useState<ToolFormState>(toFormState());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm(toFormState(editor && editor.mode === "edit" ? editor.tool : undefined));
    setSaving(false);
  }, [editor]);

  const open = Boolean(editor);

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="max-h-[80vh] overflow-y-auto p-5">
        <DialogHeader>
          <DialogTitle>{editor?.mode === "edit" ? "编辑工具" : "新建工具"}</DialogTitle>
        </DialogHeader>
        <form
          className="space-y-4 pt-2"
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
            <Field label="工具编码 *">
              <Input value={form.toolCode} onChange={(e) => setForm((prev) => ({ ...prev, toolCode: e.target.value }))} required />
            </Field>
            <Field label="工具名称 *">
              <Input value={form.toolName} onChange={(e) => setForm((prev) => ({ ...prev, toolName: e.target.value }))} required />
            </Field>
            <Field label="工具类型 *">
              <Input value={form.toolType} onChange={(e) => setForm((prev) => ({ ...prev, toolType: e.target.value }))} required />
            </Field>
            <Field label="实现 Bean">
              <Input value={form.implBean} onChange={(e) => setForm((prev) => ({ ...prev, implBean: e.target.value }))} />
            </Field>
            <Field label="HTTP/脚本端点">
              <Input value={form.endpoint} onChange={(e) => setForm((prev) => ({ ...prev, endpoint: e.target.value }))} />
            </Field>
            <Field label="超时时间(ms)">
              <Input
                type="number"
                min={0}
                value={form.timeoutMs}
                onChange={(e) => setForm((prev) => ({ ...prev, timeoutMs: e.target.value }))}
              />
            </Field>
            <Field label="绑定 MCP Server">
              <Select
                value={form.mcpServerId ? form.mcpServerId : MCP_UNBOUND_VALUE}
                onValueChange={(value) =>
                  setForm((prev) => ({ ...prev, mcpServerId: value === MCP_UNBOUND_VALUE ? "" : value }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="可选" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={MCP_UNBOUND_VALUE}>未绑定</SelectItem>
                  {mcpServers.map((mcp) => (
                    <SelectItem key={mcp.id} value={String(mcp.id)}>
                      {mcp.serverName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <Field label="启用状态">
              <div className="flex items-center gap-2 rounded-md border px-3 py-2">
                <Switch checked={form.status} onCheckedChange={(checked) => setForm((prev) => ({ ...prev, status: checked }))} />
                <span className="text-sm text-muted-foreground">{form.status ? "启用" : "停用"}</span>
              </div>
            </Field>
          </div>

          <Field label="输入 Schema(JSON)">
            <Textarea
              rows={4}
              placeholder='{"type":"object","properties":{}}'
              value={form.inputSchema}
              onChange={(e) => setForm((prev) => ({ ...prev, inputSchema: e.target.value }))}
            />
          </Field>
          <Field label="输出 Schema(JSON)">
            <Textarea
              rows={4}
              placeholder='{"type":"object","properties":{}}'
              value={form.outputSchema}
              onChange={(e) => setForm((prev) => ({ ...prev, outputSchema: e.target.value }))}
            />
          </Field>
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
