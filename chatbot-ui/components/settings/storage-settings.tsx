/**
 * 存储管理设置
 */
"use client";

import {useMemo, useState} from "react";
import useSWR from "swr";
import {toast} from "sonner";
import {
    createStorageConfig,
    deleteStorageConfig,
    listStorageConfigs,
    type StorageConfig,
    type StorageConfigCreateRequest,
    type StorageConfigUpdateRequest,
    updateStorageConfig,
} from "@/lib/api/storage-config";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
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
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {MdOutlineStar} from "react-icons/md";
import {CiCircleInfo, CiEdit, CiPower, CiTrash} from "react-icons/ci";
import {deleteFile, uploadFile} from "@/lib/api/files";

// 右侧详情项：单行省略号 + Tooltip 全文（更紧凑）
const Detail = ({ label, value }: { label: string; value?: string | null }) => {
  const text = value && String(value).trim().length > 0 ? String(value) : "-";
  const showTooltip = text !== "-";
  return (
    <div className="flex min-w-0 items-start gap-2 text-sm">
      <span className="w-20 shrink-0 text-muted-foreground">{label}:</span>
      {showTooltip ? (
        <TooltipProvider delayDuration={200}>
          <Tooltip>
            <TooltipTrigger asChild>
              <span
                className="min-w-0 flex-1 truncate text-foreground/80 cursor-default"
                title={text}
              >
                {text}
              </span>
            </TooltipTrigger>
            <TooltipContent className="max-w-[420px] break-all whitespace-pre-wrap leading-relaxed">
              {text}
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      ) : (
        <span className="min-w-0 flex-1 truncate text-foreground/80">{text}</span>
      )}
    </div>
  );
};

const Row = ({ label, tip, children }: { label: string; tip?: string; children: React.ReactNode }) => (
  <div className="grid grid-cols-[160px_1fr] items-center gap-4 border-b px-6 py-4 last:border-b-0">
    <div className="text-sm text-muted-foreground flex items-center gap-1">
      <span>{label}</span>
      {tip && (
        <TooltipProvider delayDuration={200}>
          <Tooltip>
            <TooltipTrigger asChild>
              <span className="inline-flex cursor-default items-center text-muted-foreground hover:text-foreground">
                <CiCircleInfo className="size-4" />
              </span>
            </TooltipTrigger>
            <TooltipContent className="max-w-[260px] text-xs leading-relaxed">
              {tip}
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      )}
    </div>
    <div className="flex w-full items-center justify-end gap-3">{children}</div>
  </div>
);

export function StorageSettings() {
  const { data, isLoading, mutate } = useSWR<StorageConfig[]>("storage-configs", async () => {
    return await listStorageConfigs();
  });
  const [openEdit, setOpenEdit] = useState(false);
  const [editing, setEditing] = useState<StorageConfig | null>(null);
  const [openDelete, setOpenDelete] = useState<string | null>(null);

  const sorted = useMemo(() => {
    const list = (data ?? []).slice();
    // 默认项优先，其次按 storageName 排序
    return list.sort((a, b) => (b.isDefault - a.isDefault) || a.storageName.localeCompare(b.storageName));
  }, [data]);

  return (
    <div className="flex h-full min-h-0 flex-col">
      <div className="border-b px-6 py-3">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            {/* <MdOutlineCloudDownload className="size-5" /> */}
            存储管理
          </h2>
          <TooltipProvider delayDuration={200}>
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  type="button"
                  className="inline-flex items-center text-muted-foreground hover:text-foreground"
                  aria-label="关于存储管理"
                >
                  <CiCircleInfo className="size-5" />
                </button>
              </TooltipTrigger>
              <TooltipContent className="max-w-[320px] text-xs leading-relaxed">
                管理系统可用的文件存储（如 LOCAL/S3/DB）。默认存储用于文件上传和访问；可编辑端点/区域/桶/外链等，必要时停用或删除。
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>
      </div>

      <div className="px-6 py-4 flex justify-end">
        <Button size="sm" onClick={() => { setEditing(null); setOpenEdit(true); }}>
          新增存储
        </Button>
      </div>

      <div className="divide-y">
        {!isLoading && sorted.length === 0 && (
          <div className="px-6 py-10 text-center text-sm text-muted-foreground">暂无存储配置</div>
        )}

        {sorted.map((item) => (
          <div key={item.storageCode} className="px-6 py-4 overflow-x-hidden">
            {/* 标题行 */}
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex min-w-0 items-center gap-3">
                <div className="text-base font-medium">{item.storageName}</div>
                <div className="text-xs text-muted-foreground">({item.storageCode})</div>
                <div className="text-xs rounded bg-accent px-2 py-0.5">{item.storageType}</div>
                {item.isDefault === 1 && (
                  <div className="text-xs rounded bg-[color:var(--brand-color)]/10 text-[color:var(--brand-color)] px-2 py-0.5">默认</div>
                )}
                {item.status === 0 && (
                  <div className="text-xs rounded bg-muted px-2 py-0.5 text-muted-foreground">停用</div>
                )}
              </div>
              <div className="flex items-center gap-1.5 flex-wrap">
                {item.isDefault !== 1 && (
                  <TooltipProvider delayDuration={200}>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <Button
                          size="icon"
                          variant="ghost"
                          aria-label="设为默认"
                          onClick={async () => {
                            const p = updateStorageConfig(item.storageCode, { isDefault: 1 });
                            toast.promise(p, {
                              loading: "设为默认中...",
                              success: "已设为默认",
                              error: (e) => e?.message || "设置默认失败",
                            });
                            await p;
                            mutate();
                          }}
                        >
                          <MdOutlineStar className="size-4" />
                        </Button>
                      </TooltipTrigger>
                      <TooltipContent>设为默认</TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                )}
                <TooltipProvider delayDuration={200}>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        size="icon"
                        variant="ghost"
                        aria-label={item.status === 1 ? "停用" : "启用"}
                        onClick={async () => {
                          const next = item.status === 1 ? 0 : 1;
                          const p = updateStorageConfig(item.storageCode, { status: next });
                          toast.promise(p, {
                            loading: next === 1 ? "启用中..." : "停用中...",
                            success: next === 1 ? "已启用" : "已停用",
                            error: (e) => e?.message || "操作失败",
                          });
                          await p;
                          mutate();
                        }}
                      >
                        <CiPower className="size-4" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>{item.status === 1 ? "停用" : "启用"}</TooltipContent>
                  </Tooltip>
                </TooltipProvider>
                <TooltipProvider delayDuration={200}>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        size="icon"
                        variant="ghost"
                        aria-label="编辑"
                        onClick={() => { setEditing(item); setOpenEdit(true); }}
                      >
                        <CiEdit className="size-4" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>编辑</TooltipContent>
                  </Tooltip>
                </TooltipProvider>
                <TooltipProvider delayDuration={200}>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <Button
                        size="icon"
                        variant="ghost"
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                        aria-label="删除"
                        onClick={() => setOpenDelete(item.storageCode)}
                      >
                        <CiTrash className="size-4" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent>删除</TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
            </div>

            {/* 详情行 */}
            <div className="mt-3 grid grid-cols-1 gap-y-2 gap-x-6 md:grid-cols-2 xl:grid-cols-3">
              <Detail label="Endpoint" value={item.endpoint} />
              <Detail label="Region" value={item.region} />
              <Detail label="Bucket" value={item.bucket} />
              <Detail label="BasePath" value={item.basePath} />
              <Detail label="BaseURL" value={item.baseUrl} />
              <Detail label="备注" value={item.remark} />
            </div>
          </div>
        ))}
      </div>

      {/* 新增 / 编辑对话框 */}
      {openEdit && (
        <EditStorageDialog
          open={openEdit}
          onOpenChange={setOpenEdit}
          data={editing}
          onSaved={async () => {
            await mutate();
          }}
        />
      )}

      {/* 删除确认 */}
      <AlertDialog open={!!openDelete} onOpenChange={(o) => !o && setOpenDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除该存储配置？</AlertDialogTitle>
            <AlertDialogDescription>此操作不可撤销，请谨慎操作。</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                const code = openDelete!;
                const p = deleteStorageConfig(code);
                toast.promise(p, {
                  loading: "删除中...",
                  success: "删除成功",
                  error: (e) => e?.message || "删除失败",
                });
                await p;
                setOpenDelete(null);
                mutate();
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

type EditProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  data: StorageConfig | null;
  onSaved: () => void;
};

function EditStorageDialog({ open, onOpenChange, data, onSaved }: EditProps) {
  const isEdit = !!data;
  const [form, setForm] = useState<StorageConfigCreateRequest>({
    storageCode: data?.storageCode ?? "",
    storageName: data?.storageName ?? "",
    storageType: data?.storageType ?? "S3",
    endpoint: data?.endpoint ?? "",
    region: data?.region ?? "",
    bucket: data?.bucket ?? "",
    basePath: data?.basePath ?? "",
    baseUrl: data?.baseUrl ?? "",
    accessKeyPlain: "",
    secretKeyPlain: "",
    sessionPolicy: data?.sessionPolicy ?? "",
    isDefault: data?.isDefault ?? 0,
    extConfig: data?.extConfig ?? "",
    status: data?.status ?? 1,
    remark: data?.remark ?? "",
  });

  const onSubmit = async () => {
    try {
      // 基础校验
      const codeReg = /^[A-Z0-9_-]+$/;
      const urlReg = /^https?:\/\/[\w\-.:/]+$/i;
      const isBlank = (v?: string | null) => !v || v.trim() === "";
      const jsonOptional = (label: string, v?: string | null): string | undefined => {
        if (isBlank(v)) return undefined;
        try {
          JSON.parse(v as string);
          return v as string;
        } catch {
          toast.error(`${label} 必须为合法 JSON`);
          throw new Error("invalid json");
        }
      };
      if (!form.storageCode || !codeReg.test(form.storageCode)) {
        toast.error("编码必须为大写字母、数字、下划线或中划线");
        return;
      }
      if (!form.storageName) {
        toast.error("名称不能为空");
        return;
      }
      if (form.storageType === "S3") {
        if (!form.endpoint || !urlReg.test(form.endpoint)) {
          toast.error("Endpoint 必须为合法的 http/https URL");
          return;
        }
        if (!form.region) {
          toast.error("Region 不能为空");
          return;
        }
        if (!form.bucket) {
          toast.error("Bucket 不能为空");
          return;
        }
        // 创建时必须填写访问密钥
        if (!isEdit && (!form.accessKeyPlain || !form.secretKeyPlain)) {
          toast.error("请填写 AccessKey 与 SecretKey");
          return;
        }
      }
      if (form.baseUrl && !urlReg.test(form.baseUrl)) {
        toast.error("BaseURL 必须为合法的 http/https URL");
        return;
      }
      // 规范化字段：空串 -> undefined；非 S3 类型去掉 S3 字段；JSON 字段做合法性校验
      const normalized = {
        storageCode: form.storageCode,
        storageName: form.storageName,
        storageType: form.storageType,
        endpoint: form.storageType === "S3" ? (isBlank(form.endpoint) ? undefined : form.endpoint) : undefined,
        region: form.storageType === "S3" ? (isBlank(form.region) ? undefined : form.region) : undefined,
        bucket: form.storageType === "S3" ? (isBlank(form.bucket) ? undefined : form.bucket) : undefined,
        basePath: isBlank(form.basePath) ? undefined : form.basePath,
        baseUrl: isBlank(form.baseUrl) ? undefined : form.baseUrl,
        accessKeyPlain: isBlank(form.accessKeyPlain) ? (isEdit ? "" : undefined) : form.accessKeyPlain,
        secretKeyPlain: isBlank(form.secretKeyPlain) ? (isEdit ? "" : undefined) : form.secretKeyPlain,
        sessionPolicy: jsonOptional("临时会话策略", form.sessionPolicy),
        isDefault: form.isDefault,
        extConfig: jsonOptional("扩展配置", form.extConfig),
        status: form.status,
        remark: isBlank(form.remark) ? undefined : form.remark,
      };
      if (isEdit) {
        const req: StorageConfigUpdateRequest = {
          storageName: normalized.storageName,
          storageType: normalized.storageType,
          endpoint: normalized.endpoint,
          region: normalized.region,
          bucket: normalized.bucket,
          basePath: normalized.basePath,
          baseUrl: normalized.baseUrl,
          accessKeyPlain: normalized.accessKeyPlain, // 编辑：空串表示清空，undefined 表示不修改
          secretKeyPlain: normalized.secretKeyPlain,
          sessionPolicy: normalized.sessionPolicy,
          isDefault: normalized.isDefault,
          extConfig: normalized.extConfig,
          status: normalized.status,
          remark: normalized.remark,
        };
        const p = updateStorageConfig(form.storageCode, req);
        toast.promise(p, { loading: "更新中...", success: "更新成功", error: (e) => e?.message || "更新失败" });
        await p;
      } else {
        if (!normalized.storageCode || !normalized.storageName) {
          toast.error("请填写编码和名称");
          return;
        }
        const createReq: StorageConfigCreateRequest = {
          storageCode: normalized.storageCode,
          storageName: normalized.storageName,
          storageType: normalized.storageType,
          endpoint: normalized.endpoint,
          region: normalized.region,
          bucket: normalized.bucket,
          basePath: normalized.basePath,
          baseUrl: normalized.baseUrl,
          accessKeyPlain: normalized.accessKeyPlain as string | undefined,
          secretKeyPlain: normalized.secretKeyPlain as string | undefined,
          sessionPolicy: normalized.sessionPolicy,
          isDefault: normalized.isDefault,
          extConfig: normalized.extConfig,
          status: normalized.status,
          remark: normalized.remark,
        };
        const p = createStorageConfig(createReq);
        toast.promise(p, { loading: "创建中...", success: "创建成功", error: (e) => e?.message || "创建失败" });
        await p;
      }
      onOpenChange(false);
      onSaved();
    } catch {}
  };

  // 测试连接：上传测试图片并删除
  const onTest = async () => {
    try {
      if (!form.storageCode) {
        toast.error("请先填写编码");
        return;
      }
      const res = await fetch("/images/file/test-upload.png");
      const blob = await res.blob();
      const idPromise = (async () => {
        const saved = await uploadFile({
          file: blob,
          storageCode: form.storageCode,
          bizType: "TEST_STORAGE",
          bizId: (Math.random() + 1).toString(36).substring(2),
          title: "test-upload",
        });
        try { await deleteFile(saved.id); } catch {}
      })();
      toast.promise(idPromise, {
        loading: "测试连接中...",
        success: "连接可用（上传/删除成功）",
        error: (e) => e?.message || "连接失败",
      });
      await idPromise;
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "测试失败");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {/* 固定高度 + 内部滚动，避免弹窗过高 */}
      <DialogContent className="max-w-3xl h-[80vh] p-4 sm:p-6">
        <DialogHeader>
          <DialogTitle>{isEdit ? "编辑存储" : "新增存储"}</DialogTitle>
        </DialogHeader>
        <div className="flex min-h-0 flex-1 flex-col overflow-hidden">
          <div className="mb-3 flex justify-end">
            <Button variant="secondary" size="sm" onClick={onTest}>测试连接</Button>
          </div>
          <div className="grid gap-0 overflow-y-auto">
            <Row label="编码" tip="唯一编码，用于在业务中引用该存储（不可重复）。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="唯一编码（例如 S3_MAIN）"
                value={form.storageCode}
                onChange={(e) => setForm((p) => ({ ...p, storageCode: e.target.value.toUpperCase() }))}
                readOnly={isEdit}
              />
            </Row>
            <Row label="名称" tip="用于界面展示的名称，便于识别。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="显示名称"
                value={form.storageName}
                onChange={(e) => setForm((p) => ({ ...p, storageName: e.target.value }))}
              />
            </Row>
            <Row label="类型" tip="存储实现类型：本地（LOCAL）/ 对象存储（S3）/ 数据库存储（DB）。">
              <Select
                value={form.storageType}
                onValueChange={(v) => setForm((p) => ({ ...p, storageType: v }))}
              >
                <SelectTrigger className="ml-auto max-w-[360px] w-full justify-between">
                  <SelectValue placeholder="请选择类型" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LOCAL">LOCAL</SelectItem>
                  <SelectItem value="S3">S3</SelectItem>
                  <SelectItem value="DB">DB</SelectItem>
                </SelectContent>
              </Select>
            </Row>
            {form.storageType === "S3" && (
            <Row label="Endpoint" tip="S3 兼容端点地址，例如 https://s3.amazonaws.com 或自建 MinIO 端点。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="https://s3.amazonaws.com"
                value={form.endpoint}
                onChange={(e) => setForm((p) => ({ ...p, endpoint: e.target.value }))}
              />
            </Row>
            )}
            {form.storageType === "S3" && (
            <Row label="Region" tip="区域名，如 us-east-1。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="us-east-1"
                value={form.region}
                onChange={(e) => setForm((p) => ({ ...p, region: e.target.value }))}
              />
            </Row>
            )}
            {form.storageType === "S3" && (
            <Row label="Bucket" tip="存储桶名称。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="bucket"
                value={form.bucket}
                onChange={(e) => setForm((p) => ({ ...p, bucket: e.target.value }))}
              />
            </Row>
            )}
            <Row label="BasePath" tip="对象存储路径前缀，例如 / 或 app/files。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="/"
                value={form.basePath}
                onChange={(e) => setForm((p) => ({ ...p, basePath: e.target.value }))}
              />
            </Row>
            <Row label="BaseURL" tip="用于对外访问的基础 URL（如 CDN 域名），生成可访问链接时使用。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="https://cdn.example.com"
                value={form.baseUrl}
                onChange={(e) => setForm((p) => ({ ...p, baseUrl: e.target.value }))}
              />
            </Row>
            <Row label="AccessKey" tip="访问凭证 Access Key（编辑时留空表示不修改）。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder={isEdit ? "不修改可留空" : "访问密钥"}
                value={form.accessKeyPlain ?? ""}
                onChange={(e) => setForm((p) => ({ ...p, accessKeyPlain: e.target.value }))}
              />
            </Row>
            <Row label="SecretKey" tip="访问凭证 Secret Key（编辑时留空表示不修改）。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder={isEdit ? "不修改可留空" : "访问密钥（密文）"}
                value={form.secretKeyPlain ?? ""}
                onChange={(e) => setForm((p) => ({ ...p, secretKeyPlain: e.target.value }))}
              />
            </Row>
            <Row label="默认" tip="默认存储将用于上传等场景（仅可有一个默认）。">
              <Select
                value={String(form.isDefault ?? 0)}
                onValueChange={(v) => setForm((p) => ({ ...p, isDefault: Number(v) as 0 | 1 }))}
              >
                <SelectTrigger className="ml-auto max-w-[360px] w-full justify-between">
                  <SelectValue placeholder="否" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">是</SelectItem>
                  <SelectItem value="0">否</SelectItem>
                </SelectContent>
              </Select>
            </Row>
            <Row label="状态" tip="停用后该存储不可被选择与使用。">
              <Select
                value={String(form.status ?? 1)}
                onValueChange={(v) => setForm((p) => ({ ...p, status: Number(v) as 0 | 1 }))}
              >
                <SelectTrigger className="ml-auto max-w-[360px] w-full justify-between">
                  <SelectValue placeholder="启用" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">启用</SelectItem>
                  <SelectItem value="0">停用</SelectItem>
                </SelectContent>
              </Select>
            </Row>
            <Row label="备注" tip="可选说明，便于多人协作识别。">
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="可选"
                value={form.remark ?? ""}
                onChange={(e) => setForm((p) => ({ ...p, remark: e.target.value }))}
              />
            </Row>
          </div>

          <div className="mt-4 flex justify-end gap-2">
            <Button variant="outline" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button onClick={onSubmit}>{isEdit ? "保存" : "创建"}</Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
