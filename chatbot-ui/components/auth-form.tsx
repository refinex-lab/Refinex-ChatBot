/**
 * 认证表单
 */
import Form from "next/form";

import { Input } from "./ui/input";
import { Label } from "./ui/label";

/**
 * 认证表单组件
 */
export function AuthForm({
  action,
  children,
  defaultEmail = "",
}: {
  // 表单动作
  action: NonNullable<
    string | ((formData: FormData) => void | Promise<void>) | undefined
  >;
  // 子组件
  children: React.ReactNode;
  // 默认邮箱
  defaultEmail?: string;
}) {
  return (
    // 表单
    <Form action={action} className="flex flex-col gap-4 px-4 sm:px-16">
      {/* 邮箱输入框 */}
      <div className="flex flex-col gap-2">
        {/* 邮箱标签 */}
        <Label
          className="font-normal text-zinc-600 dark:text-zinc-400"
          htmlFor="email"
        >
          邮箱地址
        </Label>

        {/* 邮箱输入框 */}
        <Input
          autoComplete="email"
          autoFocus
          className="bg-muted text-md md:text-sm"
          defaultValue={defaultEmail}
          id="email"
          name="email"
          placeholder="user@refinex.ai"
          required
          type="email"
        />
      </div>

      {/* 密码输入框 */}
      <div className="flex flex-col gap-2">
        {/* 密码标签 */}
        <Label
          className="font-normal text-zinc-600 dark:text-zinc-400"
          htmlFor="password"
        >
          密码
        </Label>

        {/* 密码输入框 */}
        <Input
          className="bg-muted text-md md:text-sm"
          id="password"
          name="password"
          required
          type="password"
        />
      </div>

      {/* 子组件 */}
      {children}
    </Form>
  );
}
