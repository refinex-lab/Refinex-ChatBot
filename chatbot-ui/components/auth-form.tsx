/**
 * 认证表单
 */
import Form from "next/form";

/**
 * 认证表单包装组件
 */
export function AuthForm({
  action,
  children,
}: {
  action: NonNullable<
    string | ((formData: FormData) => void | Promise<void>) | undefined
  >;
  children: React.ReactNode;
}) {
  return (
    <Form action={action} className="flex flex-col gap-4 px-4 sm:px-16">
      {children}
    </Form>
  );
}
