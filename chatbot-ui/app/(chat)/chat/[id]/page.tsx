import {cookies} from "next/headers";
import {notFound, redirect} from "next/navigation";

import {Chat} from "@/components/chat";
import {DataStreamHandler} from "@/components/data-stream-handler";
import {DEFAULT_CHAT_MODEL} from "@/lib/ai/models";
import {getChatById, getMessagesByChatId} from "@/lib/db/queries";
import {convertToUIMessages} from "@/lib/utils";
import {AUTH_COOKIE_NAME} from "@/lib/env";

export default async function Page(props: { params: Promise<{ id: string }> }) {
  const params = await props.params;
  const { id } = params;
  const chat = await getChatById({ id });

  if (!chat) {
    notFound();
  }

  const cookieStore = await cookies();
  const hasBackendAuth =
    Boolean(cookieStore.get(AUTH_COOKIE_NAME)?.value) ||
    Boolean(cookieStore.get("satoken")?.value);
  if (!hasBackendAuth) {
    redirect("/login");
  }

  // 前端不再校验 chat 所属，后端接口负责鉴权

  const messagesFromDb = await getMessagesByChatId({
    id,
  });

  const uiMessages = convertToUIMessages(messagesFromDb);

  const chatModelFromCookie = cookieStore.get("chat-model");

  if (!chatModelFromCookie) {
    return (
      <>
        <Chat
          autoResume={true}
          id={chat.id}
          initialChatModel={DEFAULT_CHAT_MODEL}
          initialLastContext={chat.lastContext ?? undefined}
          initialMessages={uiMessages}
          initialVisibilityType={chat.visibility}
          isReadonly={false}
        />
        <DataStreamHandler />
      </>
    );
  }

  return (
    <>
      <Chat
        autoResume={true}
        id={chat.id}
        initialChatModel={chatModelFromCookie.value}
        initialLastContext={chat.lastContext ?? undefined}
        initialMessages={uiMessages}
        initialVisibilityType={chat.visibility}
        isReadonly={false}
      />
      <DataStreamHandler />
    </>
  );
}
