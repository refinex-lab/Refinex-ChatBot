/**
 * 注册页面
 */
"use client";

import Link from "next/link";
import {useRouter} from "next/navigation";
import {useSession} from "next-auth/react";
import {useActionState, useEffect, useState} from "react";
import {AuthForm} from "@/components/auth-form";
import {AuthHeader} from "@/components/auth-header";
import {SubmitButton} from "@/components/submit-button";
import {toast} from "@/components/toast";
import {register, type RegisterActionState} from "../actions";

/**
 * 注册页面组件
 */
export default function Page() {
  const router = useRouter();

  // 邮箱
  const [email, setEmail] = useState("");
  const [isSuccessful, setIsSuccessful] = useState(false);

  // 注册状态
  const [state, formAction] = useActionState<RegisterActionState, FormData>(
    register,
    {
      status: "idle",
    }
  );

  // 更新会话
  const { update: updateSession } = useSession();

  useEffect(() => {
    // 用户已存在
    if (state.status === "user_exists") {
      toast({ type: "error", description: "账户已存在！" });
    // 注册失败
    } else if (state.status === "failed") {
      toast({ type: "error", description: "创建账户失败！" });
    // 注册数据无效
    } else if (state.status === "invalid_data") {
      toast({
        type: "error",
        description: "验证您的提交失败！",
      });
    // 注册成功
    } else if (state.status === "success") {
      toast({ type: "success", description: "账户创建成功！" });
      // 设置成功状态
      setIsSuccessful(true);
      // 更新会话
      updateSession();
      // 刷新路由
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

      {/* 注册表单容器 */}
      <div className="flex w-full max-w-md flex-col gap-12 overflow-hidden rounded-2xl">
        <div className="flex flex-col items-center justify-center gap-2 px-4 text-center sm:px-16">
          <h3 className="font-semibold text-xl dark:text-zinc-50">注册</h3>
          <p className="text-gray-500 text-sm dark:text-zinc-400">
            使用您的邮箱和密码注册
          </p>
        </div>
        <AuthForm action={handleSubmit} defaultEmail={email}>
          <SubmitButton isSuccessful={isSuccessful}>注册</SubmitButton>
          <p className="mt-4 text-center text-gray-600 text-sm dark:text-zinc-400">
            {"已经有账户？ "}
            <Link
              className="font-semibold text-gray-800 hover:underline dark:text-zinc-200"
              href="/login"
            >
              登录
            </Link>
            {" 您的账户。"}
          </p>
        </AuthForm>
      </div>
    </div>
  );
}
