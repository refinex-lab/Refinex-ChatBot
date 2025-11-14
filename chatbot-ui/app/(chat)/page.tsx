import {cookies} from "next/headers";
import {redirect} from "next/navigation";
import {Chat} from "@/components/chat";
import {DataStreamHandler} from "@/components/data-stream-handler";
import {DEFAULT_CHAT_MODEL} from "@/lib/ai/models";
import {generateUUID} from "@/lib/utils";
import {AUTH_COOKIE_NAME} from "@/lib/env";

export default async function Page() {
  const cookieStore = await cookies();
  const hasBackendAuth =
    Boolean(cookieStore.get(AUTH_COOKIE_NAME)?.value) ||
    Boolean(cookieStore.get("satoken")?.value);
  if (!hasBackendAuth) redirect("/login");

  const id = generateUUID();

  const modelIdFromCookie = cookieStore.get("chat-model");

  if (!modelIdFromCookie) {
    return (
      <>
        <Chat
          autoResume={false}
          id={id}
          initialChatModel={DEFAULT_CHAT_MODEL}
          initialMessages={[]}
          initialVisibilityType="private"
          isReadonly={false}
          key={id}
        />
        <DataStreamHandler />
      </>
    );
  }

  return (
    <>
      <Chat
        autoResume={false}
        id={id}
        initialChatModel={modelIdFromCookie.value}
        initialMessages={[]}
        initialVisibilityType="private"
        isReadonly={false}
        key={id}
      />
      <DataStreamHandler />
    </>
  );
}
