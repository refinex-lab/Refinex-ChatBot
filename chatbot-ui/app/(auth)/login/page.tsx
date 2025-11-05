/**
 * 登录页面
 */
"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useSession } from "next-auth/react";
import { useActionState, useEffect, useState } from "react";

import { AuthForm } from "@/components/auth-form";
import { SubmitButton } from "@/components/submit-button";
import { toast } from "@/components/toast";
import { type LoginActionState, login } from "../actions";

/**
 * 登录页面组件
 */
export default function Page() {
  const router = useRouter();

  // 邮箱
  const [email, setEmail] = useState("");
  const [isSuccessful, setIsSuccessful] = useState(false);

  // 登录状态
  const [state, formAction] = useActionState<LoginActionState, FormData>(
    login,
    {
      status: "idle",
    }
  );

  // 更新会话
  const { update: updateSession } = useSession();

  useEffect(() => {
    // 登录失败
    if (state.status === "failed") {
      toast({
        type: "error",
        description: "无效的凭证！",
      });
    // 登录数据无效
    } else if (state.status === "invalid_data") {
      toast({
        type: "error",
        description: "验证您的提交失败！",
      });
    // 登录成功
    } else if (state.status === "success") {
      setIsSuccessful(true);
      updateSession();
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
    <div className="flex h-dvh w-screen items-start justify-center bg-background pt-12 md:items-center md:pt-0">
      <div className="flex w-full max-w-md flex-col gap-12 overflow-hidden rounded-2xl">
        <div className="flex flex-col items-center justify-center gap-2 px-4 text-center sm:px-16">
          <h3 className="font-semibold text-xl dark:text-zinc-50">登录</h3>
          <p className="text-gray-500 text-sm dark:text-zinc-400">
            使用您的邮箱和密码登录
          </p>
        </div>
        <AuthForm action={handleSubmit} defaultEmail={email}>
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
