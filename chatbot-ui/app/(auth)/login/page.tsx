/**
 * 登录页面
 */
"use client";

import Link from "next/link";
import {useRouter} from "next/navigation";
import {useActionState, useCallback, useEffect, useState} from "react";

import {AuthForm} from "@/components/auth-form";
import {AuthHeader} from "@/components/auth-header";
import {SubmitButton} from "@/components/submit-button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {toast} from "@/components/toast";
import type {CaptchaPayload} from "@/lib/api/captcha";
import {requestCaptcha} from "@/lib/api/captcha";
import {login, type LoginActionState} from "../actions";

/**
 * 登录页面组件
 */
export default function Page() {
  const router = useRouter();

  // 邮箱
  const [email, setEmail] = useState("");
  const [isSuccessful, setIsSuccessful] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [captcha, setCaptcha] = useState<CaptchaPayload | null>(null);
  const [captchaLoading, setCaptchaLoading] = useState(false);

  // 登录状态
  const [state, formAction] = useActionState<LoginActionState, FormData>(
    login,
    {
      status: "idle",
    }
  );


  const loadCaptcha = useCallback(async () => {
    setCaptchaLoading(true);
    try {
      const result = await requestCaptcha();
      const image = result.image.startsWith("data:image")
        ? result.image
        : `data:image/png;base64,${result.image}`;
      setCaptcha({ ...result, image });
    } catch (error) {
      toast({
        type: "error",
        description:
          error instanceof Error ? error.message : "获取验证码失败，请稍后重试",
      });
    } finally {
      setCaptchaLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCaptcha();
  }, [loadCaptcha]);

  useEffect(() => {
    // 登录失败
    if (state.status === "failed") {
      toast({
        type: "error",
        description: state.message ?? "无效的凭证！",
      });
      loadCaptcha();
    // 登录数据无效
    } else if (state.status === "invalid_data") {
      toast({
        type: "error",
        description: state.message ?? "验证您的提交失败！",
      });
      loadCaptcha();
    // 登录成功
    } else if (state.status === "success") {
      setIsSuccessful(true);
      router.refresh();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state.status]);

  // 提交表单
  const handleSubmit = (formData: FormData) => {
    setEmail(formData.get("email") as string);
    formAction(formData);
  };

  return (
    <div className="relative flex h-dvh w-screen items-start justify-center bg-background pt-12 md:items-center md:pt-0">
      {/* 左上角 Logo 和标题 */}
      <div className="absolute top-6 left-6">
        <AuthHeader />
      </div>

      {/* 登录表单容器 */}
      <div className="flex w-full max-w-md flex-col gap-12 overflow-hidden rounded-2xl">
        <div className="flex flex-col items-center justify-center gap-2 px-4 text-center sm:px-16">
          <h3 className="font-semibold text-xl dark:text-zinc-50">登录</h3>
          <p className="text-gray-500 text-sm dark:text-zinc-400">
            使用您的邮箱和密码登录
          </p>
        </div>
        <AuthForm action={handleSubmit}>
          <div className="flex flex-col gap-2">
            <Label
              className="font-normal text-zinc-600 dark:text-zinc-400"
              htmlFor="email"
            >
              邮箱地址
            </Label>
            <Input
              autoComplete="email"
              autoFocus
              className="bg-muted text-md md:text-sm"
              defaultValue={email}
              id="email"
              name="email"
              placeholder="user@refinex.ai"
              required
              type="email"
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label
              className="font-normal text-zinc-600 dark:text-zinc-400"
              htmlFor="password"
            >
              密码
            </Label>

            <Input
              className="bg-muted text-md md:text-sm"
              id="password"
              name="password"
              required
              type="password"
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label
              className="font-normal text-zinc-600 dark:text-zinc-400"
              htmlFor="captchaCode"
            >
              验证码
            </Label>

            <div className="flex items-center gap-3">
              <Input
                autoComplete="off"
                className="bg-muted text-md md:text-sm"
                id="captchaCode"
                maxLength={8}
                name="captchaCode"
                placeholder="输入验证码"
                required
              />
              <button
                className="flex h-10 w-28 items-center justify-center rounded-lg border bg-background p-1 text-sm text-gray-600 outline-none transition hover:border-gray-400 dark:bg-zinc-900"
                onClick={loadCaptcha}
                type="button"
              >
                {captchaLoading || !captcha ? (
                  <span>加载中</span>
                ) : (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    alt="验证码"
                    className="h-full w-full rounded-md object-contain"
                    src={captcha.image}
                  />
                )}
              </button>
            </div>
            <input name="captchaUuid" type="hidden" value={captcha?.uuid ?? ""} />
          </div>

          <label className="flex items-center gap-2 text-gray-600 text-sm dark:text-zinc-400">
            <input
              checked={rememberMe}
              className="size-4 accent-zinc-900 dark:accent-zinc-100"
              name="rememberMe"
              onChange={event => setRememberMe(event.target.checked)}
              type="checkbox"
            />
            记住我
          </label>

          <SubmitButton isSuccessful={isSuccessful}>登录</SubmitButton>
          <p className="mt-4 text-center text-gray-600 text-sm dark:text-zinc-400">
            {"没有账户？ "}
            <Link
              className="font-semibold text-gray-800 hover:underline dark:text-zinc-200"
              href="/register"
            >
              注册
            </Link>
            {" 您的账户。"}
          </p>
        </AuthForm>
      </div>
    </div>
  );
}
