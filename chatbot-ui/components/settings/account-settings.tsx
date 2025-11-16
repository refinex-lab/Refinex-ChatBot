/**
 * 账户设置（个人信息 / 修改密码）
 */
"use client";

import {useEffect, useRef, useState} from "react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {toast} from "sonner";
import {uploadFile} from "@/lib/api/files";
import {PLATFORM_FILES_BASE_URL} from "@/lib/env";
import {changePassword, updateProfile, type UpdateProfileBody, useUserProfile} from "@/lib/api/user";
import {AvatarCropper} from "@/components/settings/avatar-cropper";
import {Eye, EyeOff} from "lucide-react";

const DEFAULT_AVATAR = "/images/user/default-avatar.svg";

export function AccountSettings() {
  const { data, mutate } = useUserProfile();

  const [profile, setProfile] = useState<UpdateProfileBody>({
    nickname: "",
    sex: undefined,
    avatar: "",
    email: "",
    mobile: "",
  });
  const [pwd, setPwd] = useState({ oldPassword: "", newPassword: "", confirmPassword: "" });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPwd, setSavingPwd] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [cropSrc, setCropSrc] = useState<string | null>(null);
  const initializedRef = useRef<boolean>(false);
  const [showEmail, setShowEmail] = useState<boolean>(false);
  const [showMobile, setShowMobile] = useState<boolean>(false);

  // 初始化表单
  const avatarUrl = data?.avatar || DEFAULT_AVATAR;
  useEffect(() => {
    // 只在首次获取到数据时填充，避免覆盖用户已输入但未保存的修改
    if (data && !initializedRef.current) {
      setProfile({
        nickname: data.nickname ?? "",
        sex: (data.sex as any) ?? undefined,
        avatar: data.avatar ?? "",
        email: data.email ?? "",
        mobile: data.mobile ?? "",
      });
      initializedRef.current = true;
    }
  }, [data]);

  const onPickAvatar = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const dataUrl = await readFileAsDataURL(file);
      setCropSrc(dataUrl);
    } catch (e) {
      console.error(e);
    }
  };

  // Canvas 中心裁剪为正方形
  async function cropImageToSquare(file: File, size = 512): Promise<Blob> {
    const dataUrl = await readFileAsDataURL(file);
    const img = await loadImage(dataUrl);
    const side = Math.min(img.width, img.height);
    const sx = Math.floor((img.width - side) / 2);
    const sy = Math.floor((img.height - side) / 2);
    const canvas = document.createElement("canvas");
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d")!;
    ctx.drawImage(img, sx, sy, side, side, 0, 0, size, size);
    const blob: Blob = await new Promise((resolve) =>
      canvas.toBlob((b) => resolve(b || file), "image/jpeg", 0.92)
    );
    return blob;
  }
  function readFileAsDataURL(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const fr = new FileReader();
      fr.onload = () => resolve(fr.result as string);
      fr.onerror = reject;
      fr.readAsDataURL(file);
    });
  }
  function loadImage(src: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = reject;
      img.src = src;
    });
  }
  const onSaveProfile = async () => {
    setSavingProfile(true);
    try {
      const body: UpdateProfileBody = {
        nickname: profile.nickname || data?.nickname || undefined,
        sex: (profile.sex as any) || (data?.sex as any) || undefined,
        avatar: profile.avatar || data?.avatar || undefined,
        email: profile.email || data?.email || undefined,
        mobile: profile.mobile || data?.mobile || undefined,
      };
      const saving = updateProfile(body);
      toast.promise(saving, {
        loading: "保存中...",
        success: "保存成功",
        error: (e) => e?.message || "保存失败",
      });
      const updated = await saving;
      // 本地表单直接同步为服务器返回值，避免被清空
      setProfile({
        nickname: updated.nickname ?? "",
        sex: (updated.sex as any) ?? undefined,
        avatar: updated.avatar ?? "",
        email: updated.email ?? "",
        mobile: updated.mobile ?? "",
      });
      // 同步刷新 SWR 数据（侧边栏联动）
      try {
        await mutate();
      } catch {}
    } finally {
      setSavingProfile(false);
    }
  };

  const onSavePassword = async () => {
    if (!pwd.oldPassword || !pwd.newPassword || !pwd.confirmPassword) {
      toast.error("请完整填写密码信息");
      return;
    }
    setSavingPwd(true);
    try {
      const saving = changePassword(pwd);
      toast.promise(saving, {
        loading: "修改密码中...",
        success: "密码修改成功，即将退出登录",
        error: (e) => e?.message || "密码修改失败",
      });
      await saving;
      try {
        await fetch("/api/auth/logout", { method: "POST" });
      } catch {}
      window.location.href = "/login";
      setPwd({ oldPassword: "", newPassword: "", confirmPassword: "" });
    } finally {
      setSavingPwd(false);
    }
  };

  return (
    <>
    <div>
      <div className="border-b px-6 py-3">
        <h2 className="text-lg font-semibold">账户</h2>
      </div>

      <div className="space-y-8 p-6">
          {/* 资料：一行一个设置项，行与行之间浅色分隔 */}
          <div className="divide-y">
            {/* 头像 */}
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">头像</div>
              <div className="flex items-center gap-4">
                <img
                  src={profile.avatar || avatarUrl}
                  alt="avatar"
                  width={64}
                  height={64}
                  className="h-16 w-16 rounded-full border object-cover"
                />
                <input
                  ref={fileInputRef}
                  id="avatar"
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={onPickAvatar}
                />
                <Button size="sm" variant="secondary" onClick={() => fileInputRef.current?.click()}>
                  更换
                </Button>
              </div>
            </div>

            {/* 昵称 */}
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">昵称</div>
              <Input
                className="ml-auto max-w-[360px]"
                placeholder="请输入昵称"
                value={profile.nickname ?? ""}
                onChange={(e) => setProfile((p) => ({ ...p, nickname: e.target.value }))}
              />
            </div>

            {/* 性别 */}
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">性别</div>
              <Select
                value={profile.sex ?? (data?.sex as any) ?? undefined}
                onValueChange={(v) => setProfile((p) => ({ ...p, sex: v as any }))}
              >
                <SelectTrigger className="ml-auto max-w-[360px] w-full justify-between">
                  <SelectValue placeholder="请选择" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MALE">男</SelectItem>
                  <SelectItem value="FEMALE">女</SelectItem>
                  <SelectItem value="OTHER">其他</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* 邮箱 */}
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">邮箱</div>
              <div className="relative ml-auto w-full max-w-[360px]">
                <Input
                  type="text"
                  className="pr-10"
                  placeholder="name@example.com"
                  value={showEmail ? (profile.email ?? "") : maskEmail(profile.email)}
                  readOnly={!showEmail}
                  onChange={(e) => {
                    if (showEmail) setProfile((p) => ({ ...p, email: e.target.value }));
                  }}
                />
                <button
                  type="button"
                  aria-label={showEmail ? "隐藏邮箱" : "显示邮箱"}
                  onClick={() => setShowEmail((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showEmail ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            {/* 手机号 */}
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">手机号</div>
              <div className="relative ml-auto w-full max-w-[360px]">
                <Input
                  type="text"
                  className="pr-10"
                  placeholder="请输入手机号"
                  value={showMobile ? (profile.mobile ?? "") : maskMobile(profile.mobile)}
                  readOnly={!showMobile}
                  onChange={(e) => {
                    if (showMobile) setProfile((p) => ({ ...p, mobile: e.target.value }));
                  }}
                />
                <button
                  type="button"
                  aria-label={showMobile ? "隐藏手机号" : "显示手机号"}
                  onClick={() => setShowMobile((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                >
                  {showMobile ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            {/* 保存按钮行（不画分隔线） */}
            <div className="flex justify-end py-3">
              <Button onClick={onSaveProfile} disabled={savingProfile}>
                保存
              </Button>
            </div>
          </div>

          {/* 修改密码 */}
          <div className="space-y-4">
            <div className="border-b pb-2">
              <h3 className="text-base font-medium">修改密码</h3>
            </div>
            <div className="divide-y">
              <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">原密码</div>
              <Input
                type="password"
                className="ml-auto max-w-[360px]"
                value={pwd.oldPassword}
                onChange={(e) => setPwd((p) => ({ ...p, oldPassword: e.target.value }))}
              />
            </div>
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">新密码</div>
              <Input
                type="password"
                className="ml-auto max-w-[360px]"
                value={pwd.newPassword}
                onChange={(e) => setPwd((p) => ({ ...p, newPassword: e.target.value }))}
              />
            </div>
            <div className="grid grid-cols-[160px_1fr] items-center gap-4 py-3">
              <div className="px-2 text-sm text-muted-foreground">确认新密码</div>
              <Input
                type="password"
                className="ml-auto max-w-[360px]"
                value={pwd.confirmPassword}
                onChange={(e) => setPwd((p) => ({ ...p, confirmPassword: e.target.value }))}
              />
            </div>
            </div>
            <div className="flex justify-end py-3">
              <Button className="hover:bg-primary hover:text-primary-foreground" variant="secondary" onClick={onSavePassword} disabled={savingPwd}>
                修改密码
              </Button>
            </div>
          </div>
      </div>
    </div>
      {cropSrc && (
        <AvatarCropper
          open={true}
          src={cropSrc}
          onClose={() => setCropSrc(null)}
        onCropped={async (blob) => {
          const uploading = uploadFile({
            file: blob,
            bizType: "USER_AVATAR",
            title: "avatar",
            compress: true,
            maxWidth: 512,
            quality: 0.9,
          });
          toast.promise(uploading, {
            loading: "上传头像中...",
            success: "头像上传成功",
            error: "头像上传失败",
          });
          const res = await uploading;
          const url = res.uri && /^https?:\/\//i.test(res.uri)
            ? res.uri
            : (res.id ? `${PLATFORM_FILES_BASE_URL}/${res.id}/download` : "");
          setProfile((prev) => ({ ...prev, avatar: url }));
          setCropSrc(null);
        }}
      />
      )}
    </>
  );
}

// 脱敏：邮箱，仅显示首尾与域名
function maskEmail(v?: string | null): string {
  if (!v) return "";
  const at = v.indexOf("@");
  if (at <= 0) return "****";
  const local = v.slice(0, at);
  const domain = v.slice(at);
  if (local.length <= 2) return local[0] + "****" + domain;
  return local[0] + "****" + local.slice(-1) + domain;
}

// 脱敏：手机号，保留前三后四
function maskMobile(v?: string | null): string {
  if (!v) return "";
  const s = v.replace(/\s+/g, "");
  if (s.length <= 7) return s[0] + "****" + s.slice(-2);
  return s.slice(0, 3) + "****" + s.slice(-4);
}
