import {cookies} from "next/headers";
import Script from "next/script";
import {AppSidebar} from "@/components/app-sidebar";
import {CodePreviewWrapper} from "@/components/code-preview-wrapper";
import {DataStreamProvider} from "@/components/data-stream-provider";
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";

export const experimental_ppr = true;

export default async function Layout({
  children,
}: {
  children: React.ReactNode;
}) {
  const cookieStore = await cookies();
  const isCollapsed = cookieStore.get("sidebar_state")?.value !== "true";
  const email = cookieStore.get("RX_EMAIL")?.value;
  const user = email ? { email } : undefined;

  return (
    <>
      <Script
        src="https://cdn.jsdelivr.net/pyodide/v0.23.4/full/pyodide.js"
        strategy="beforeInteractive"
      />
      <DataStreamProvider>
        <CodePreviewWrapper>
          <SidebarProvider defaultOpen={!isCollapsed}>
            <AppSidebar user={user} />
            <SidebarInset>{children}</SidebarInset>
          </SidebarProvider>
        </CodePreviewWrapper>
      </DataStreamProvider>
    </>
  );
}
