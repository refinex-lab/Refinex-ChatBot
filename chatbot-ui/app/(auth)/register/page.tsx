/**
 * 注册页面
 */
"use client";

import Image from "next/image";
import Link from "next/link";
import {useRouter} from "next/navigation";
import {useActionState, useCallback, useEffect, useState} from "react";

import {AuthForm} from "@/components/auth-form";
import {AuthHeader} from "@/components/auth-header";
import {SubmitButton} from "@/components/submit-button";
import {Card, CardContent} from "@/components/ui/card";
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {toast} from "@/components/toast";
import type {CaptchaPayload} from "@/lib/api/captcha";
import {requestCaptcha} from "@/lib/api/captcha";
import {register, type RegisterActionState} from "../actions";

export default function Page() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [isSuccessful, setIsSuccessful] = useState(false);
  const [captcha, setCaptcha] = useState<CaptchaPayload | null>(null);
  const [captchaLoading, setCaptchaLoading] = useState(false);
  const [termsOpen, setTermsOpen] = useState(false);
  const [privacyOpen, setPrivacyOpen] = useState(false);

  const [state, formAction] = useActionState<RegisterActionState, FormData>(register, {
    status: "idle",
  });

  const loadCaptcha = useCallback(async () => {
    setCaptchaLoading(true);
    try {
      const result = await requestCaptcha();
      const image = result.image.startsWith("data:image") ? result.image : `data:image/png;base64,${result.image}`;
      setCaptcha({ ...result, image });
    } catch (error) {
      toast({ type: "error", description: error instanceof Error ? error.message : "获取验证码失败，请稍后重试" });
    } finally {
      setCaptchaLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCaptcha();
  }, [loadCaptcha]);

  useEffect(() => {
    if (state.status === "user_exists") {
      toast({ type: "error", description: state.message ?? "账户已存在！" });
      loadCaptcha();
    } else if (state.status === "failed") {
      toast({ type: "error", description: state.message ?? "创建账户失败！" });
      loadCaptcha();
    } else if (state.status === "invalid_data") {
      toast({ type: "error", description: state.message ?? "验证您的提交失败！" });
      loadCaptcha();
    } else if (state.status === "success") {
      toast({ type: "success", description: "账户创建成功，请登录！" });
      setIsSuccessful(true);
      setTimeout(() => {
        router.push("/login");
      }, 900);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.status]);

  const handleSubmit = (formData: FormData) => {
    setEmail(formData.get("email") as string);
    formAction(formData);
  };

  return (
    <div className="relative flex min-h-dvh w-full items-center justify-center bg-gradient-to-br from-background via-muted/40 to-background px-4 py-10">
      <div className="absolute left-4 top-4 sm:left-8 sm:top-8">
        <AuthHeader />
      </div>

      <Card className="w-full max-w-5xl overflow-hidden border border-border/40 bg-background shadow-[0px_20px_80px_rgba(15,23,42,0.08)]">
        <CardContent className="grid p-0 md:grid-cols-[1.05fr_0.95fr]">
          <div className="p-6 sm:p-10">
            <div className="space-y-2 text-center md:text-left">
              <h1 className="text-2xl font-semibold text-foreground">注册 Refinex ChatBot</h1>
              <p className="text-sm text-muted-foreground">搭建企业级 AI 协作平台，统一管理模型与知识资产。</p>
            </div>
            <AuthForm action={handleSubmit} className="mt-8 mx-auto flex w-full max-w-[460px] flex-col gap-5 px-0">
              <div className="space-y-2">
                <Label className="text-sm text-muted-foreground" htmlFor="username">
                  用户名
                </Label>
                <Input
                  autoComplete="off"
                  autoFocus
                  className="bg-transparent"
                  id="username"
                  minLength={3}
                  name="username"
                  placeholder="输入用户名"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label className="text-sm text-muted-foreground" htmlFor="email">
                  邮箱地址
                </Label>
                <Input
                  autoComplete="email"
                  className="bg-transparent"
                  defaultValue={email}
                  id="email"
                  name="email"
                  placeholder="user@refinex.ai"
                  required
                  type="email"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-sm text-muted-foreground" htmlFor="password">
                  密码
                </Label>
                <Input className="bg-transparent" id="password" minLength={8} name="password" required type="password" />
              </div>

              <div className="space-y-2">
                <Label className="text-sm text-muted-foreground" htmlFor="confirmPassword">
                  确认密码
                </Label>
                <Input className="bg-transparent" id="confirmPassword" minLength={8} name="confirmPassword" required type="password" />
              </div>

              <div className="space-y-2">
                <Label className="text-sm text-muted-foreground" htmlFor="captchaCode">
                  验证码
                </Label>
                <div className="flex items-center gap-3">
                  <Input
                    autoComplete="off"
                    className="bg-transparent"
                    id="captchaCode"
                    maxLength={8}
                    name="captchaCode"
                    placeholder="输入验证码"
                    required
                  />
                  <button
                    className="flex h-12 w-32 items-center justify-center rounded-lg border bg-background/80 p-2 text-sm text-muted-foreground transition hover:border-foreground/40"
                    onClick={loadCaptcha}
                    type="button"
                  >
                    {captchaLoading || !captcha ? (
                      <span>加载中</span>
                    ) : (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img alt="验证码" className="h-full w-full rounded-md object-contain" src={captcha.image} />
                    )}
                  </button>
                </div>
                <input name="captchaUuid" type="hidden" value={captcha?.uuid ?? ""} />
              </div>

              <SubmitButton isSuccessful={isSuccessful}>注册</SubmitButton>
              <p className="text-center text-xs text-muted-foreground">
                注册即表示您同意我们的{" "}
                <button type="button" className="underline underline-offset-4" onClick={() => setTermsOpen(true)}>
                  服务条款
                </button>{" "}
                与{" "}
                <button type="button" className="underline underline-offset-4" onClick={() => setPrivacyOpen(true)}>
                  隐私政策
                </button>
                。
              </p>
            </AuthForm>
            <p className="mt-6 text-center text-sm text-muted-foreground">
              已经有账户？
              <Link className="ml-1 font-semibold text-foreground hover:underline" href="/login">
                立即登录
              </Link>
            </p>
          </div>
          <div className="relative hidden min-h-[420px] overflow-hidden bg-muted md:block">
            <Image
              src="/images/login/login-bg.jpg"
              alt="注册背景"
              fill
              className="object-cover"
              priority
            />
          </div>
        </CardContent>
      </Card>

      <Dialog open={termsOpen} onOpenChange={setTermsOpen}>
        <DialogContent className="max-w-xl space-y-4 p-6">
          <DialogHeader>
            <DialogTitle>服务条款</DialogTitle>
            <DialogDescription asChild>
              <div className="space-y-3 text-sm text-muted-foreground">
                <p>Refinex ChatBot 依据 Apache License 2.0 发布，支持自由部署、修改与商用。使用本平台时请遵循以下约定：</p>
                <ul className="list-disc space-y-1 pl-5 text-xs">
                  <li>仅在合法授权的业务范围内调用模型、知识库与文件能力，不得从事违规内容。</li>
                  <li>妥善保管账号、密钥等敏感凭证，如发现异常需及时吊销并反馈。</li>
                  <li>遵守各模型供应商的配额、速率及内容政策，自行承担商用合规责任。</li>
                  <li>上传或同步的数据需具备合法权利，平台仅在授权范围内处理与存储。</li>
                  <li>向社区回馈代码或脚本，默认遵循 Apache License 2.0 的衍生作品条款。</li>
                </ul>
              </div>
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      </Dialog>

      <Dialog open={privacyOpen} onOpenChange={setPrivacyOpen}>
        <DialogContent className="max-w-xl space-y-4 p-6">
          <DialogHeader>
            <DialogTitle>隐私政策</DialogTitle>
            <DialogDescription asChild>
              <div className="space-y-3 text-sm text-muted-foreground">
                <p>我们倡导私有化部署方案，所有业务数据由您掌控。平台遵循以下隐私原则：</p>
                <ul className="list-disc space-y-1 pl-5 text-xs">
                  <li>仅收集身份认证与审计追踪所需的最少信息，不会向第三方出售或传输。</li>
                  <li>验证码、口令及模型密钥全程加密存储，管理员亦无法明文查看。</li>
                  <li>调用日志仅用于安全审计与问题排查，可按策略自动清理。</li>
                  <li>若需删除账户或导出数据，可随时联系管理员，我们将及时响应。</li>
                  <li>上传的知识库或对话内容默认保存在您的基础设施中，不会用于训练或共享。</li>
                </ul>
              </div>
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      </Dialog>
    </div>
  );
}
