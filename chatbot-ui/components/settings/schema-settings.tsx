"use client";

import {type ReactNode, useEffect, useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {LuBookCopy, LuPenLine, LuPlus, LuRefreshCcw, LuTrash2} from "react-icons/lu";
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
    AiSchema,
    createSchema,
    deleteSchema,
    querySchemas,
    SchemaMutationPayload,
    SchemaQuery,
    updateSchema,
    updateSchemaStatus,
} from "@/lib/api/ai/schema";

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "启用" },
  { value: "0", label: "停用" },
];

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; schema: AiSchema };

type SchemaFormState = {
  schemaCode: string;
  schemaName: string;
  schemaType: string;
  schemaJson: string;
  version: string;
  strictMode: boolean;
  status: boolean;
  remark: string;
};

function toFormState(schema?: AiSchema): SchemaFormState {
  if (!schema) {
    return {
      schemaCode: "",
      schemaName: "",
      schemaType: "",
      schemaJson: "",
      version: "",
      strictMode: false,
      status: true,
      remark: "",
    };
  }
  return {
    schemaCode: schema.schemaCode ?? "",
    schemaName: schema.schemaName ?? "",
    schemaType: schema.schemaType ?? "",
    schemaJson: schema.schemaJson ? JSON.stringify(schema.schemaJson, null, 2) : "",
    version: schema.version?.toString() ?? "",
    strictMode: (schema.strictMode ?? 0) === 1,
    status: (schema.status ?? 1) === 1,
    remark: schema.remark ?? "",
  };
}

function buildPayload(form: SchemaFormState): SchemaMutationPayload {
  const payload: SchemaMutationPayload = {
    schemaCode: form.schemaCode.trim(),
    schemaName: form.schemaName.trim(),
    schemaType: form.schemaType.trim(),
    version: form.version ? Number(form.version) : undefined,
    strictMode: form.strictMode ? 1 : 0,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
  if (form.schemaJson) {
    try {
      payload.schemaJson = JSON.parse(form.schemaJson);
    } catch {
      throw new Error("Schema JSON 格式不正确");
    }
  }
  return payload;
}

export function SchemaSettings() {
  const [filters, setFilters] = useState({ keyword: "", schemaType: "", status: "all" });
  const [pagination, setPagination] = useState({ current: 1, size: 6 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiSchema | null>(null);
  const [statusPendingId, setStatusPendingId] = useState<number | null>(null);

  const queryParams: SchemaQuery = useMemo(
    () => ({
      keyword: filters.keyword || undefined,
      schemaType: filters.schemaType || undefined,
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    }),
    [filters, pagination]
  );

  const swrKey = useMemo(() => ["ai-schemas", queryParams] as const, [queryParams]);
  const { data, error, isLoading, mutate, isValidating } = useSWR(swrKey, () => querySchemas(queryParams));

  const schemas = data?.records ?? [];
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

  const handleSubmit = async (form: SchemaFormState) => {
    let payload: SchemaMutationPayload;
    try {
      payload = buildPayload(form);
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Schema 解析失败");
      return;
    }
    if (!payload.schemaCode || !payload.schemaName || !payload.schemaType) {
      toast.error("请完善编码/名称/类型");
      return;
    }
    try {
      if (editor?.mode === "edit") {
        await updateSchema(editor.schema.id, payload);
        toast.success(`已更新 Schema ${editor.schema.schemaName}`);
      } else {
        await createSchema(payload);
        toast.success("已创建 Schema");
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
      await deleteSchema(pendingDelete.id);
      toast.success(`已删除 ${pendingDelete.schemaName}`);
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
          <LuBookCopy className="size-5 text-primary" />
          Schema 管理
        </div>
        <p className="text-xs text-muted-foreground">集中维护结构化输出、工具入参等 JSON Schema，支撑 Agent 约束和校验。</p>
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
              placeholder="Schema 类型"
              value={filters.schemaType}
              onChange={(e) => handleFilter("schemaType", e.target.value)}
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
              新建 Schema
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">Schema JSON 支持完整 JSON Schema 语法，可配合严格模式输出。</p>
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
                    <th className="w-[30%] px-5 py-3 font-medium">Schema</th>
                    <th className="w-[35%] px-5 py-3 font-medium">说明</th>
                    <th className="w-[10%] px-5 py-3 text-center font-medium">版本</th>
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
                  {!isLoading && schemas.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-5 py-12 text-center text-sm text-muted-foreground">
                        暂无 Schema
                      </td>
                    </tr>
                  )}
                  {schemas.map((schema) => (
                    <tr key={schema.id} className="border-t border-border/60">
                      <td className="px-5 py-3">
                        <div className="space-y-1">
                          <div className="font-medium">{schema.schemaName}</div>
                          <div className="text-xs text-muted-foreground">{schema.schemaCode}</div>
                          <div className="text-xs text-muted-foreground">{schema.schemaType}</div>
                        </div>
                      </td>
                      <td className="px-5 py-3 text-xs text-muted-foreground">
                        {schema.remark ? (
                          <p className="line-clamp-2">{schema.remark}</p>
                        ) : (
                          <span className="text-muted-foreground/70">-</span>
                        )}
                        <p className="text-[11px] text-muted-foreground">
                          Strict: {schema.strictMode === 1 ? "ON" : "OFF"} / JSON:{" "}
                          {schema.schemaJson ? Object.keys(schema.schemaJson).length : 0}个字段
                        </p>
                      </td>
                      <td className="px-5 py-3 text-center text-xs text-muted-foreground">{schema.version ?? "-"}</td>
                      <td className="px-5 py-3 text-center">
                        <Switch
                          checked={schema.status === 1}
                          disabled={statusPendingId === schema.id}
                          onCheckedChange={async (checked) => {
                            setStatusPendingId(schema.id);
                            try {
                              await updateSchemaStatus(schema.id, checked ? 1 : 0);
                              toast.success(`已${checked ? "启用" : "停用"} ${schema.schemaName}`);
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
                          <Button variant="ghost" size="icon" onClick={() => setEditor({ mode: "edit", schema })}>
                            <LuPenLine className="size-4" />
                          </Button>
                          <Button variant="ghost" size="icon" onClick={() => setPendingDelete(schema)}>
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

      <SchemaEditorDialog editor={editor} onClose={() => setEditor(null)} onSubmit={handleSubmit} />

      <AlertDialog open={Boolean(pendingDelete)} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除 Schema ？</AlertDialogTitle>
            <AlertDialogDescription>
              删除后 Agent/工具绑定的 Schema 需要重新配置。是否继续删除 <span className="font-semibold">{pendingDelete?.schemaName}</span>？
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

function SchemaEditorDialog({
  editor,
  onClose,
  onSubmit,
}: {
  editor: EditorState | null;
  onClose: () => void;
  onSubmit: (form: SchemaFormState) => void;
}) {
  const [form, setForm] = useState<SchemaFormState>(toFormState());
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setForm(toFormState(editor && editor.mode === "edit" ? editor.schema : undefined));
    setSaving(false);
  }, [editor]);

  const open = Boolean(editor);

  return (
    <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
      <DialogContent className="max-h-[80vh] overflow-y-auto p-5">
        <DialogHeader>
          <DialogTitle>{editor?.mode === "edit" ? "编辑 Schema" : "新建 Schema"}</DialogTitle>
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
            <Field label="Schema 编码 *">
              <Input value={form.schemaCode} required onChange={(e) => setForm((prev) => ({ ...prev, schemaCode: e.target.value }))} />
            </Field>
            <Field label="Schema 名称 *">
              <Input value={form.schemaName} required onChange={(e) => setForm((prev) => ({ ...prev, schemaName: e.target.value }))} />
            </Field>
            <Field label="Schema 类型 *">
              <Input value={form.schemaType} required onChange={(e) => setForm((prev) => ({ ...prev, schemaType: e.target.value }))} />
            </Field>
            <Field label="版本号">
              <Input
                type="number"
                min={1}
                value={form.version}
                onChange={(e) => setForm((prev) => ({ ...prev, version: e.target.value }))}
              />
            </Field>
            <Field label="严格模式">
              <div className="flex items-center gap-2 rounded-md border px-3 py-2">
                <Switch checked={form.strictMode} onCheckedChange={(checked) => setForm((prev) => ({ ...prev, strictMode: checked }))} />
                <span className="text-sm text-muted-foreground">{form.strictMode ? "开启" : "关闭"}</span>
              </div>
            </Field>
            <Field label="启用状态">
              <div className="flex items-center gap-2 rounded-md border px-3 py-2">
                <Switch checked={form.status} onCheckedChange={(checked) => setForm((prev) => ({ ...prev, status: checked }))} />
                <span className="text-sm text-muted-foreground">{form.status ? "启用" : "停用"}</span>
              </div>
            </Field>
          </div>

          <Field label="Schema JSON">
            <Textarea
              rows={8}
              placeholder='{"type":"object","properties":{"field":{"type":"string"}}}'
              value={form.schemaJson}
              onChange={(e) => setForm((prev) => ({ ...prev, schemaJson: e.target.value }))}
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
