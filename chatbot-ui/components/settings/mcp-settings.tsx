"use client";

import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuCable, LuPenLine, LuPlus, LuRefreshCcw, LuTrash2} from "react-icons/lu";
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
    AiMcpServer,
    createMcpServer,
    deleteMcpServer,
    McpMutationPayload,
    McpQuery,
    queryMcpServers,
    updateMcpServer,
    updateMcpServerStatus,
} from "@/lib/api/ai/mcp-server";

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

const TRANSPORT_TYPES = [
  { value: "stdio", label: "Stdio" },
  { value: "http", label: "HTTP" },
  { value: "websocket", label: "WebSocket" },
];

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; server: AiMcpServer };

type McpFormState = {
  serverCode: string;
  serverName: string;
  transportType: string;
  entryCommand: string;
  endpointUrl: string;
  manifestUrl: string;
  authType: string;
  authSecretCipher: string;
  authSecretIndex: string;
  toolsFilter: string;
  status: boolean;
  remark: string;
};

function toFormState(server?: AiMcpServer): McpFormState {
  if (!server) {
    return {
      serverCode: "",
      serverName: "",
      transportType: "",
      entryCommand: "",
      endpointUrl: "",
      manifestUrl: "",
      authType: "",
      authSecretCipher: "",
      authSecretIndex: "",
      toolsFilter: "",
      status: true,
      remark: "",
    };
  }
  return {
    serverCode: server.serverCode ?? "",
    serverName: server.serverName ?? "",
    transportType: server.transportType ?? "",
    entryCommand: server.entryCommand ?? "",
    endpointUrl: server.endpointUrl ?? "",
    manifestUrl: server.manifestUrl ?? "",
    authType: server.authType ?? "",
    authSecretCipher: server.authSecretCipher ?? "",
    authSecretIndex: server.authSecretIndex ?? "",
    toolsFilter: server.toolsFilter ?? "",
    status: (server.status ?? 1) === 1,
    remark: server.remark ?? "",
  };
}

function toPayload(form: McpFormState): McpMutationPayload {
  return {
    serverCode: form.serverCode.trim(),
    serverName: form.serverName.trim(),
    transportType: form.transportType.trim(),
    entryCommand: form.entryCommand.trim() || undefined,
    endpointUrl: form.endpointUrl.trim() || undefined,
    manifestUrl: form.manifestUrl.trim() || undefined,
    authType: form.authType.trim() || undefined,
    authSecretCipher: form.authSecretCipher.trim() || undefined,
    authSecretIndex: form.authSecretIndex.trim() || undefined,
    toolsFilter: form.toolsFilter.trim() || undefined,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
}

export function McpSettings() {
  const [filters, setFilters] = useState({ keyword: "", status: "all", transportType: "all" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiMcpServer | null>(null);
  const [statusPendingId, setStatusPendingId] = useState<number | null>(null);

  const queryParams: McpQuery = useMemo(
    () => ({
      keyword: filters.keyword || undefined,
      transportType: filters.transportType === "all" ? undefined : filters.transportType,
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    }),
    [filters, pagination]
  );

  const swrKey = useMemo(() => ["ai-mcp", queryParams] as const, [queryParams]);
  const { data, error, isLoading, mutate, isValidating } = useSWR(swrKey, () => queryMcpServers(queryParams));

  const servers = data?.records ?? [];
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

  const handleSubmit = async (form: McpFormState) => {
    const payload = toPayload(form);
    if (!payload.serverCode || !payload.serverName || !payload.transportType) {
      toast.error("请完善必填字段");
      return;
    }
    try {
      if (editor?.mode === "edit") {
        await updateMcpServer(editor.server.id, payload);
        toast.success(`已更新 MCP Server ${editor.server.serverName}`);
      } else {
        await createMcpServer(payload);
        toast.success("已创建 MCP Server");
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
      await deleteMcpServer(pendingDelete.id);
      toast.success(`已删除 ${pendingDelete.serverName}`);
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
          <LuCable className="size-5 text-primary" />
          MCP Server 管理
        </div>
        <p className="text-xs text-muted-foreground">集中管理外部工具/服务的 MCP Endpoint，供工具与 Agent 调用。</p>
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
            <Select value={filters.transportType} onValueChange={(value) => handleFilter("transportType", value)}>
              <SelectTrigger className="w-44">
                <SelectValue placeholder="传输类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部类型</SelectItem>
                {TRANSPORT_TYPES.map((type) => (
                  <SelectItem key={type.value} value={type.value}>
                    {type.label}
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
              新建 MCP Server
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">支持 stdio/HTTP/WebSocket 多种实现方式，并可配置鉴权信息。</p>
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
                    <th className="w-[30%] px-5 py-3 font-medium">Server</th>
                    <th className="w-[33%] px-5 py-3 font-medium">Endpoint / Manifest</th>
                    <th className="w-[15%] px-5 py-3 text-center font-medium">传输</th>
                    <th className="w-[10%] px-5 py-3 text-center font-medium">状态</th>
                    <th className="w-[12%] px-5 py-3 text-center font-medium">操作</th>
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
                  {!isLoading && servers.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无配置
                      </td>
                    </tr>
                  )}
                  {servers.map((server) => (
                    <tr key={server.id} className="border-t border-border/60">
                      <td className="px-5 py-3">
                        <div className="space-y-1">
                          <div className="font-medium">{server.serverName}</div>
                          <div className="text-xs text-muted-foreground">{server.serverCode}</div>
                          {server.remark && <p className="text-xs text-muted-foreground line-clamp-2">{server.remark}</p>}
                        </div>
                      </td>
                      <td className="px-5 py-3 text-xs text-muted-foreground">
                        <p className="truncate">{server.endpointUrl || "-"}</p>
                        <p className="truncate">{server.manifestUrl || "-"}</p>
                      </td>
                      <td className="px-5 py-3 text-center text-xs text-muted-foreground uppercase">{server.transportType || "-"}</td>
                      <td className="px-5 py-3 text-center">
                        <Switch
                          checked={server.status === 1}
                          disabled={statusPendingId === server.id}
                          onCheckedChange={async (checked) => {
                            setStatusPendingId(server.id);
                            try {
                              await updateMcpServerStatus(server.id, checked ? 1 : 0);
                              toast.success(`已${checked ? "启用" : "停用"} ${server.serverName}`);
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
                          <Button variant="ghost" size="icon" onClick={() => setEditor({ mode: "edit", server })}>
                            <LuPenLine className="size-4" />
                          </Button>
                          <Button variant="ghost" size="icon" onClick={() => setPendingDelete(server)}>
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

      <McpEditorDialog editor={editor} onClose={() => setEditor(null)} onSubmit={handleSubmit} />

      <AlertDialog open={Boolean(pendingDelete)} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除 MCP Server ？</AlertDialogTitle>
            <AlertDialogDescription>
              删除后依赖该 Server 的工具将不可用，请确认 <span className="font-semibold">{pendingDelete?.serverName}</span> 不再使用。
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

function McpEditorDialog({
  editor,
  onClose,
  onSubmit,
}: {
  editor: EditorState | null;
  onClose: () => void;
  onSubmit: (form: McpFormState) => void;
}) {
  const [form, setForm] = useState<McpFormState>(toFormState());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm(toFormState(editor && editor.mode === "edit" ? editor.server : undefined));
    setSaving(false);
  }, [editor]);

  const open = Boolean(editor);

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="max-h-[80vh] overflow-y-auto p-5">
        <DialogHeader>
          <DialogTitle>{editor?.mode === "edit" ? "编辑 MCP Server" : "新建 MCP Server"}</DialogTitle>
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
            <Field label="Server 编码 *">
              <Input value={form.serverCode} required onChange={(e) => setForm((prev) => ({ ...prev, serverCode: e.target.value }))} />
            </Field>
            <Field label="Server 名称 *">
              <Input value={form.serverName} required onChange={(e) => setForm((prev) => ({ ...prev, serverName: e.target.value }))} />
            </Field>
            <Field label="传输类型 *">
              <Select
                value={form.transportType || ""}
                onValueChange={(value) => setForm((prev) => ({ ...prev, transportType: value }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="选择传输方式" />
                </SelectTrigger>
                <SelectContent>
                  {TRANSPORT_TYPES.map((item) => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <Field label="工具白名单(逗号分隔)">
              <Input value={form.toolsFilter} onChange={(e) => setForm((prev) => ({ ...prev, toolsFilter: e.target.value }))} />
            </Field>
            <Field label="入口命令">
              <Input value={form.entryCommand} onChange={(e) => setForm((prev) => ({ ...prev, entryCommand: e.target.value }))} />
            </Field>
            <Field label="Endpoint URL">
              <Input value={form.endpointUrl} onChange={(e) => setForm((prev) => ({ ...prev, endpointUrl: e.target.value }))} />
            </Field>
            <Field label="Manifest URL">
              <Input value={form.manifestUrl} onChange={(e) => setForm((prev) => ({ ...prev, manifestUrl: e.target.value }))} />
            </Field>
            <Field label="鉴权类型">
              <Input value={form.authType} onChange={(e) => setForm((prev) => ({ ...prev, authType: e.target.value }))} />
            </Field>
            <Field label="鉴权密钥密文">
              <Input value={form.authSecretCipher} onChange={(e) => setForm((prev) => ({ ...prev, authSecretCipher: e.target.value }))} />
            </Field>
            <Field label="鉴权密钥索引">
              <Input value={form.authSecretIndex} onChange={(e) => setForm((prev) => ({ ...prev, authSecretIndex: e.target.value }))} />
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
