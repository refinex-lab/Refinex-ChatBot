/**
 * 认证表单
 */
import Form from "next/form";
import {cn} from "@/lib/utils";

/**
 * 认证表单包装组件
 */
export function AuthForm({
  action,
  children,
  className,
}: {
  action: NonNullable<
    string | ((formData: FormData) => void | Promise<void>) | undefined
  >;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <Form action={action} className={cn("flex flex-col gap-4 px-4 sm:px-8", className)}>
      {children}
    </Form>
  );
}
