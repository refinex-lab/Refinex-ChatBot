/**
 * 提示词管理设置页
 */
"use client";

import {TextareaHTMLAttributes, useEffect, useMemo, useState} from "react";
import {pinyin} from "pinyin-pro";
import useSWR from "swr";
import {toast} from "sonner";
import {Badge} from "@/components/ui/badge";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Textarea} from "@/components/ui/textarea";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Dialog, DialogClose, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog";
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
import {HoverCard, HoverCardContent, HoverCardTrigger} from "@/components/ui/hover-card";
import {ScrollArea} from "@/components/ui/scroll-area";
import {Switch} from "@/components/ui/switch";
import {Skeleton} from "@/components/ui/skeleton";
import {
    AiPrompt,
    createPrompt,
    deletePrompt,
    PromptMutationPayload,
    queryPrompts,
    updatePrompt,
} from "@/lib/api/ai/prompts";
import {LuBookOpen, LuCopy, LuRefreshCcw, LuSparkles, LuTrash2} from "react-icons/lu";
import {X} from "lucide-react";
import {TbPencilMinus} from "react-icons/tb";

const PROMPT_FORMATS = [
  { value: "SPRING", label: "结构化表达式", hint: "支持 if/for 等表达式，适合复杂模板。" },
  { value: "MUSTACHE", label: "Mustache 模板", hint: "语法直观，内容同学也能轻松维护。" },
  { value: "STRING", label: "占位符模板", hint: "通过 {变量} 替换，快速得到成品话术。" },
  { value: "LITERAL", label: "固定文本", hint: "直接输出原文，多用于公告或工具提示。" },
];

const PROMPT_ROLES = [
  { value: "system", label: "系统提示" },
  { value: "user", label: "用户输入" },
  { value: "assistant", label: "助手回答" },
  { value: "tool", label: "工具输出" },
];

const STATUS_FILTERS = [
  { value: "all", label: "全部" },
  { value: "1", label: "可用" },
  { value: "0", label: "暂停" },
];

const SUGGESTED_CATEGORIES = ["常规助手", "检索问答", "活动运营", "工具指令", "客服答疑"];

type EditorState =
  | { mode: "create" }
  | { mode: "edit"; prompt: AiPrompt };

export function PromptSettings() {
  const [filters, setFilters] = useState({
    keyword: "",
    category: "",
    format: "all",
    status: "all",
  });
  const [pagination, setPagination] = useState({ current: 1, size: 8 });
  const [editor, setEditor] = useState<EditorState | null>(null);
  const [pendingDelete, setPendingDelete] = useState<AiPrompt | null>(null);

  const swrKey = useMemo(
    () => ["ai-prompts", filters, pagination] as const,
    [filters, pagination]
  );
  const { data, error, isLoading, mutate, isValidating } = useSWR(swrKey, () =>
    queryPrompts({
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      templateFormat: filters.format === "all" ? undefined : filters.format,
      status: filters.status === "all" ? undefined : Number(filters.status),
      current: pagination.current,
      size: pagination.size,
    })
  );

  const prompts = data?.records ?? [];
  const total = data?.total ?? 0;

  const handleSearchChange = (value: string) => {
    setFilters((prev) => ({ ...prev, keyword: value }));
    setPagination((prev) => ({ ...prev, current: 1 }));
  };

  const handleFilterChange = (key: "category" | "format" | "status", value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPagination((prev) => ({ ...prev, current: 1 }));
  };

  const handleRefresh = () => mutate();

  const handleDeleteConfirm = async () => {
    if (!pendingDelete) return;
    try {
      await deletePrompt(pendingDelete.id);
      toast.success(`已删除提示词 ${pendingDelete.promptName}`);
      setPendingDelete(null);
      mutate();
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "删除提示词失败");
    }
  };

  const totalPages = data?.pages ?? 1;

  return (
    <div className="flex h-full flex-col">
      <div className="border-b px-6 py-3">
        <div className="flex items-center gap-2 text-lg font-semibold">
          {/* <MdOutlineTipsAndUpdates className="size-5 text-amber-500" /> */}
          提示词管理
        </div>
        {/* <p className="text-sm text-muted-foreground">
          将 AI 的 Prompt 能力在一个地方可视化管理，为业务、内容、运营提供可迭代的提示词资产。
        </p> */}
      </div>

      <div className="flex-1 overflow-y-auto px-6 py-5 space-y-6">
        <div className="rounded-2xl border bg-background/60 p-4 shadow-sm space-y-4">
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex-1 min-w-[200px]">
              <Input
                placeholder="搜索提示词、编码或描述…"
                value={filters.keyword}
                onChange={(e) => handleSearchChange(e.target.value)}
              />
            </div>
            <Input
              className="w-40"
              placeholder="分类"
              value={filters.category}
              onChange={(e) => handleFilterChange("category", e.target.value)}
            />
            <Select
              value={filters.format}
              onValueChange={(value) => handleFilterChange("format", value)}
            >
              <SelectTrigger className="w-48">
                <SelectValue placeholder="模板格式" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部格式</SelectItem>
                {PROMPT_FORMATS.map((fmt) => (
                  <SelectItem key={fmt.value} value={fmt.value}>
                    {fmt.label}
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
                  onClick={() => handleFilterChange("status", item.value)}
                >
                  {item.label}
                </button>
              ))}
            </div>
            <Button variant="outline" size="sm" onClick={handleRefresh} className="gap-1">
              <LuRefreshCcw className={`size-4 ${isValidating ? "animate-spin" : ""}`} />
              刷新
            </Button>
            <div className="flex items-center gap-2">
              <Button size="sm" className="gap-1" onClick={() => setEditor({ mode: "create" })}>
                <LuSparkles className="mr-1 size-4" />
                新建提示词
              </Button>
              <PromptDemoHint />
            </div>
          </div>
          <p className="text-xs text-muted-foreground">
            小提示：分类可以用自然语言描述（如「常规问答」「活动报名」），方便团队快速识别。
          </p>
        </div>

        <div className="rounded-2xl border bg-card/60 p-1 shadow-sm">
          {error && (
            <div className="rounded-2xl border border-destructive/40 bg-destructive/5 px-6 py-8 text-center text-sm text-destructive">
              加载失败：{error instanceof Error ? error.message : "未知错误"}
              <div className="mt-4">
                <Button size="sm" variant="secondary" onClick={handleRefresh}>
                  重试
                </Button>
              </div>
            </div>
          )}

          {!error && (
            <div className="space-y-3 p-2">
              {isLoading && (
                <div className="space-y-3">
                  {Array.from({ length: 3 }).map((_, idx) => (
                    <Skeleton key={idx} className="h-32 rounded-2xl" />
                  ))}
                </div>
              )}
              {!isLoading && prompts.length === 0 && (
                <div className="rounded-2xl border border-dashed px-8 py-16 text-center">
                  <p className="text-base font-medium">还没有提示词</p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    点击右上角「新建提示词」或导入已有模板，构建你专属的 Prompt 库。
                  </p>
                </div>
              )}
              {!isLoading &&
                prompts.map((prompt) => (
                  <PromptCard
                    key={prompt.id}
                    prompt={prompt}
                    onEdit={() => setEditor({ mode: "edit", prompt })}
                    onDelete={() => setPendingDelete(prompt)}
                  />
                ))}
            </div>
          )}

          {prompts.length > 0 && (
            <div className="flex items-center justify-between border-t px-4 py-3 text-sm text-muted-foreground">
              <div>
                共 <strong>{total}</strong> 条提示词，当前第 {pagination.current}/{totalPages} 页
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={pagination.current <= 1}
                  onClick={() => setPagination((prev) => ({ ...prev, current: Math.max(1, prev.current - 1) }))}
                >
                  上一页
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={!data?.hasNext}
                  onClick={() => setPagination((prev) => ({ ...prev, current: prev.current + 1 }))}
                >
                  下一页
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>

      <PromptEditorDialog
        state={editor}
        open={!!editor}
        onClose={() => setEditor(null)}
        onSuccess={() => mutate()}
      />

      <AlertDialog open={!!pendingDelete} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除提示词？</AlertDialogTitle>
            <AlertDialogDescription>
              将删除 <strong>{pendingDelete?.promptName}</strong>（{pendingDelete?.promptCode}），操作不可恢复。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={handleDeleteConfirm}>
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

function PromptDemoHint() {
  const template = `你是一个中英文翻译专家。对用户的输入进行翻译：
- 如果输入为中文，输出对应的英文译文。
- 如果输入为英文，输出自然流畅的中文译文。
- 译文需兼顾“信、达、雅”，可根据上下文调整语气与风格。

待翻译内容：{{content}}`;
  const exampleInput =
    "牛顿第一定律：任何一个物体总是保持静止状态或者匀速直线运动状态，直到有作用在它上面的外力迫使它改变这种状态为止。";
  const exampleOutput =
    "Newton's First Law: An object will remain at rest or in uniform straight-line motion unless acted upon by an external force that compels it to change this state.";

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(template);
      toast.success("示例模板已复制");
    } catch {
      toast.error("复制失败，请手动选择文本");
    }
  };

  return (
    <HoverCard openDelay={100}>
      <HoverCardTrigger asChild>
        <Button type="button" variant="ghost" size="sm" className="gap-1 text-muted-foreground">
          <LuBookOpen className="size-4" />
          示例提示词
        </Button>
      </HoverCardTrigger>
      <HoverCardContent
        align="end"
        sideOffset={12}
        className="z-50 w-[420px] space-y-3 text-sm shadow-xl"
      >
        <div className="flex items-center justify-between">
          <p className="font-medium text-foreground">中英文互译</p>
          <Button variant="outline" size="sm" className="gap-1" onClick={handleCopy}>
            <LuCopy className="size-3" />
            复制
          </Button>
        </div>
        <div>
          <p className="text-xs uppercase text-muted-foreground">系统提示</p>
          <pre className="mt-1 max-h-40 overflow-auto rounded-lg bg-muted/40 p-2 text-xs leading-relaxed text-muted-foreground whitespace-pre-wrap break-words">
            {template}
          </pre>
        </div>
        <div className="rounded-lg bg-muted/30 p-2 text-xs leading-relaxed text-muted-foreground">
          <p className="font-medium text-foreground">示例输入</p>
          <p>{exampleInput}</p>
          <p className="mt-2 font-medium text-foreground">示例输出</p>
          <p>{exampleOutput}</p>
        </div>
      </HoverCardContent>
    </HoverCard>
  );
}

function PromptCard({ prompt, onEdit, onDelete }: { prompt: AiPrompt; onEdit: () => void; onDelete: () => void }) {
  const formatMeta = PROMPT_FORMATS.find((item) => item.value === prompt.templateFormat);
  const roleLabel = PROMPT_ROLES.find((item) => item.value === prompt.role)?.label ?? prompt.role;
  const statusBadge =
    prompt.status === 1 ? (
      <Badge className="bg-emerald-500/15 text-emerald-700 hover:bg-emerald-500/40">可用</Badge>
    ) : (
      <Badge variant="outline" className="text-muted-foreground">暂停中</Badge>
    );

  return (
    <div className="rounded-2xl border bg-background/80 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex flex-wrap items-start gap-3 border-b px-5 py-4">
        <div className="flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-base font-semibold">{prompt.promptName}</p>
            <Badge variant="outline" className="text-xs">
              {prompt.promptCode}
            </Badge>
            {statusBadge}
          </div>
          <p className="mt-1 text-sm text-muted-foreground line-clamp-2">{prompt.description || "暂无描述"}</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="ghost" size="sm" onClick={onEdit}>
            <TbPencilMinus className="mr-1 size-4" />
            编辑
          </Button>
          <Button variant="ghost" size="sm" className="text-destructive hover:text-destructive" onClick={onDelete}>
            <LuTrash2 className="mr-1 size-4" />
            删除
          </Button>
        </div>
      </div>

      <div className="grid gap-4 px-5 py-4 md:grid-cols-[2fr_1fr]">
        <div>
          <div className="text-xs uppercase text-muted-foreground">模板预览</div>
          <pre className="mt-2 max-h-44 overflow-auto rounded-xl bg-muted/40 p-3 text-sm leading-relaxed text-muted-foreground whitespace-pre-wrap break-words">
            {prompt.template}
          </pre>
        </div>
        <div className="space-y-2 text-sm text-muted-foreground">
          <div className="rounded-xl border bg-background/80 p-3">
            <div className="text-xs font-medium text-foreground">核心信息</div>
            <div className="mt-2 flex flex-wrap gap-2">
              <Badge variant="outline">{prompt.category || "未分类"}</Badge>
              <Badge variant="outline">{formatMeta?.label ?? prompt.templateFormat}</Badge>
              <Badge variant="outline">{roleLabel}</Badge>
            </div>
          </div>
          <div className="rounded-xl border bg-background/80 p-3 text-xs">
            <div className="font-medium text-foreground">备注 / 示例</div>
            <p className="mt-1 line-clamp-3">
              {prompt.remark || "建议在模板中加入【语气、结构、风险提示】等说明，让模型生成更稳定。"}
            </p>
          </div>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-3 border-t px-5 py-3 text-xs text-muted-foreground">
        <span>最近更新：{prompt.updateTime || "未知"}</span>
        <span>创建人：{prompt.createBy ?? "-"}</span>
        <span>内容指纹：{prompt.hashSha256?.slice(0, 12) ?? "-"}</span>
      </div>
    </div>
  );
}

type PromptEditorDialogProps = {
  state: EditorState | null;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
};

const emptyForm = {
  promptCode: "",
  promptName: "",
  category: "",
  description: "",
  templateFormat: "SPRING",
  role: "system",
  template: "",
  variablesText: "",
  examplesText: "",
  inputSchemaText: "",
  remark: "",
  status: true,
};

function PromptEditorDialog({ state, open, onClose, onSuccess }: PromptEditorDialogProps) {
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);
  const [autoCodeEnabled, setAutoCodeEnabled] = useState(true);

  useEffect(() => {
    if (!open) {
      setForm(emptyForm);
      return;
    }
    if (state?.mode === "edit" && state.prompt) {
      const p = state.prompt;
      setForm({
        promptCode: p.promptCode,
        promptName: p.promptName,
        category: p.category ?? "",
        description: p.description ?? "",
        templateFormat: p.templateFormat,
        role: p.role,
        template: p.template,
        variablesText: stringifyJson(p.variables),
        examplesText: stringifyJson(p.examples),
        inputSchemaText: stringifyJson(p.inputSchema),
        remark: p.remark ?? "",
        status: p.status !== 0,
      });
      setAutoCodeEnabled(false);
    } else {
      setForm(emptyForm);
      setAutoCodeEnabled(true);
    }
  }, [open, state]);

  const handleChange =
    (key: keyof typeof form) =>
    (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setForm((prev) => ({ ...prev, [key]: event.target.value }));
    };

  const handleNameChange = (value: string) => {
    setForm((prev) => ({
      ...prev,
      promptName: value,
      promptCode: autoCodeEnabled ? buildPromptCode(value) : prev.promptCode,
    }));
  };

  const handleCodeChange = (value: string) => {
    setAutoCodeEnabled(false);
    setForm((prev) => ({ ...prev, promptCode: value }));
  };

  const regenerateCode = () => {
    setAutoCodeEnabled(true);
    setForm((prev) => ({ ...prev, promptCode: buildPromptCode(prev.promptName) }));
  };

  const applyCategoryChip = (chip: string) => {
    setForm((prev) => ({ ...prev, category: chip }));
  };

  const handleSubmit = async () => {
    const payloadResult = buildPayload(form);
    if (payloadResult.error) {
      toast.error(payloadResult.error);
      return;
    }
    const payload = payloadResult.payload!;
    setSubmitting(true);
    try {
      if (state?.mode === "edit" && state.prompt) {
        await updatePrompt(state.prompt.id, payload);
        toast.success("提示词已更新");
      } else {
        await createPrompt(payload);
        toast.success("提示词已创建");
      }
      onClose();
      onSuccess();
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "保存失败");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(next) => {
      if (!next && !submitting) {
        onClose();
      }
    }}>
      <DialogContent className="flex h-[80vh] max-w-5xl flex-col overflow-hidden p-0">
        <DialogClose asChild>
          <button
            aria-label="关闭"
            className="absolute right-4 top-4 rounded-full p-1 text-muted-foreground hover:bg-accent"
          >
            <X className="size-4" />
          </button>
        </DialogClose>
        <DialogHeader className="border-b px-6 py-4 pr-12">
          <DialogTitle>{state?.mode === "edit" ? "编辑提示词" : "新建提示词"}</DialogTitle>
          {/* <DialogDescription>用平实的语言描述用途和输入，让每位同事都能维护这条提示词。</DialogDescription> */}
        </DialogHeader>
        <ScrollArea className="flex-1">
          <div className="px-6">
          <form
            className="grid gap-4 py-5"
            onSubmit={(e) => {
              e.preventDefault();
              void handleSubmit();
            }}
          >
            <div className="grid gap-4 md:grid-cols-2">
              <div className="grid gap-2">
                <label className="text-sm font-medium text-foreground">
                  提示词名称 <span className="text-destructive">*</span>
                </label>
                <Input
                  required
                  placeholder="例：客服常见问题助手"
                  value={form.promptName}
                  onChange={(e) => handleNameChange(e.target.value)}
                />
                <p className="text-xs text-muted-foreground">一句话描述用途，便于搜索与协作。</p>
              </div>
              <div className="grid gap-2">
                <label className="text-sm font-medium text-foreground">
                  提示词编码 <span className="text-destructive">*</span>
                </label>
                <div className="flex gap-2">
                  <Input
                    required
                    placeholder="自动生成，也可修改"
                    value={form.promptCode}
                    onChange={(e) => handleCodeChange(e.target.value)}
                  />
                  <Button type="button" variant="outline" size="sm" onClick={regenerateCode}>
                    自动生成
                  </Button>
                </div>
                <p className="text-xs text-muted-foreground">编码仅用于系统识别，使用英文或数字即可。</p>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="grid gap-2">
                <label className="text-sm font-medium text-foreground">使用场景标签</label>
                <Input
                  placeholder="例：活动报名、检索问答"
                  value={form.category}
                  onChange={(e) => setForm((prev) => ({ ...prev, category: e.target.value }))}
                />
                <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
                  <span>常用：</span>
                  {SUGGESTED_CATEGORIES.map((chip) => (
                    <button
                      type="button"
                      key={chip}
                      className="rounded-full border px-3 py-0.5 hover:bg-accent/60"
                      onClick={() => applyCategoryChip(chip)}
                    >
                      {chip}
                    </button>
                  ))}
                </div>
              </div>
              <div className="grid gap-2">
                <label className="text-sm font-medium text-foreground">模板形式</label>
                <Select value={form.templateFormat} onValueChange={(value) => setForm((prev) => ({ ...prev, templateFormat: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择模板形式" />
                  </SelectTrigger>
                  <SelectContent>
                    {PROMPT_FORMATS.map((fmt) => (
                      <SelectItem key={fmt.value} value={fmt.value}>
                        {fmt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-xs text-muted-foreground">
                  {PROMPT_FORMATS.find((fmt) => fmt.value === form.templateFormat)?.hint}
                </p>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="grid gap-2">
                <label className="text-sm font-medium text-foreground">扮演角色</label>
                <Select value={form.role} onValueChange={(value) => setForm((prev) => ({ ...prev, role: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择角色" />
                  </SelectTrigger>
                  <SelectContent>
                    {PROMPT_ROLES.map((role) => (
                      <SelectItem key={role.value} value={role.value}>
                        {role.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid gap-2">
                <label className="text-sm font-medium text-foreground">状态</label>
                <div className="flex items-center gap-3 rounded-full border bg-muted/40 px-4 py-2">
                  <Switch
                    checked={form.status}
                    onCheckedChange={(checked) => setForm((prev) => ({ ...prev, status: checked }))}
                  />
                  <span className="text-sm text-muted-foreground">{form.status ? "可用" : "暂停"}</span>
                </div>
              </div>
            </div>

            <TextareaField
              label="简单描述"
              value={form.description}
              onChange={handleChange("description")}
              placeholder="例如：用于客服 FAQ，语气亲切，无法回答时提醒转人工。"
            />
            <TextareaField
              label="模板正文"
              value={form.template}
              onChange={handleChange("template")}
              placeholder="写清角色、要点、输出格式，可使用 {{变量}}。"
              rows={8}
              required
            />
            <TextareaField
              label="变量示例（JSON）"
              value={form.variablesText}
              onChange={handleChange("variablesText")}
              placeholder='例如：{ "tone": "温和", "language": "zh-CN" }'
              rows={4}
            />
            <TextareaField
              label="示例对话（JSON 数组）"
              value={form.examplesText}
              onChange={handleChange("examplesText")}
              placeholder='例如：[ { "input": "用户提问", "output": "期望回答" } ]'
              rows={4}
            />
            <TextareaField
              label="输入字段（JSON Schema）"
              value={form.inputSchemaText}
              onChange={handleChange("inputSchemaText")}
              placeholder='例如：{ "type": "object", "properties": { "question": { "type": "string" } } }'
              rows={4}
            />
            <TextareaField
              label="备注"
              value={form.remark}
              onChange={handleChange("remark")}
              placeholder="记录版本、灰度批次等信息。"
              rows={3}
            />
          </form>
          </div>
        </ScrollArea>
        <div className="border-t bg-background px-6 py-4">
          <div className="flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={onClose} disabled={submitting}>
              取消
            </Button>
            <Button type="button" onClick={handleSubmit} disabled={submitting}>
              {submitting ? "保存中…" : "保存"}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

function TextareaField({
  label,
  required,
  ...props
}: { label: string; required?: boolean } & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <div className="grid gap-2">
      <label className="text-sm font-medium text-foreground">
        {label} {required && <span className="text-destructive">*</span>}
      </label>
      <Textarea {...props} />
    </div>
  );
}

function stringifyJson(value: unknown) {
  if (!value) return "";
  try {
    if (Array.isArray(value) && value.length === 0) return "";
    if (typeof value === "object" && Object.keys(value as Record<string, unknown>).length === 0) return "";
    return JSON.stringify(value, null, 2);
  } catch {
    return "";
  }
}

function buildPayload(form: typeof emptyForm): { payload?: PromptMutationPayload; error?: string } {
  const base: PromptMutationPayload = {
    promptCode: form.promptCode.trim(),
    promptName: form.promptName.trim(),
    category: form.category.trim() || undefined,
    description: form.description.trim() || undefined,
    templateFormat: form.templateFormat,
    role: form.role,
    template: form.template,
    status: form.status ? 1 : 0,
    remark: form.remark.trim() || undefined,
  };
  if (!base.promptCode) return { error: "请填写提示词编码" };
  if (!base.promptName) return { error: "请填写提示词名称" };
  if (!base.template.trim()) return { error: "模板正文不能为空" };

  try {
    if (form.variablesText.trim()) {
      const parsed = JSON.parse(form.variablesText);
      if (typeof parsed !== "object" || Array.isArray(parsed)) throw new Error("变量示例需要是 JSON 对象");
      base.variables = parsed;
    }
  } catch (error) {
    return { error: error instanceof Error ? error.message : "变量示例 JSON 解析失败" };
  }

  try {
    if (form.examplesText.trim()) {
      const parsed = JSON.parse(form.examplesText);
      if (!Array.isArray(parsed)) throw new Error("示例对话需为 JSON 数组");
      base.examples = parsed as Array<Record<string, unknown>>;
    }
  } catch (error) {
    return { error: error instanceof Error ? error.message : "示例对话 JSON 解析失败" };
  }

  try {
    if (form.inputSchemaText.trim()) {
      const parsed = JSON.parse(form.inputSchemaText);
      if (typeof parsed !== "object" || Array.isArray(parsed)) throw new Error("输入 Schema 需是 JSON 对象");
      base.inputSchema = parsed;
    }
  } catch (error) {
    return { error: error instanceof Error ? error.message : "输入 Schema JSON 解析失败" };
  }

  return { payload: base };
}

function buildPromptCode(source: string) {
  const text = source.trim();
  if (!text) return "";
  try {
    const arr = pinyin(text, { toneType: "none", type: "array", nonZh: "spaced" }) as string[];
    const slug = arr
      .join("-")
      .replace(/[^a-zA-Z0-9-]/g, "-")
      .replace(/-+/g, "-")
      .replace(/^-|-$/g, "");
    if (slug) {
      return slug.toLowerCase();
    }
  } catch {
    // ignore
  }
  return text
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "");
}
