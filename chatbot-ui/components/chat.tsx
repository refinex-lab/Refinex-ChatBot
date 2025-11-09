"use client";

import {useChat} from "@ai-sdk/react";
import {DefaultChatTransport} from "ai";
import {useSearchParams} from "next/navigation";
import {useEffect, useRef, useState} from "react";
import {PanelGroup, Panel, PanelResizeHandle} from "react-resizable-panels";
import useSWR, {useSWRConfig} from "swr";
import {unstable_serialize} from "swr/infinite";
import {ChatHeader} from "@/components/chat-header";
import {useArtifactSelector} from "@/hooks/use-artifact";
import {useAutoResume} from "@/hooks/use-auto-resume";
import {useChatVisibility} from "@/hooks/use-chat-visibility";
import type {Vote} from "@/lib/db/schema";
import {ChatSDKError} from "@/lib/errors";
import type {Attachment, ChatMessage} from "@/lib/types";
import type {AppUsage} from "@/lib/usage";
import {fetcher, fetchWithErrorHandlers, generateUUID} from "@/lib/utils";
import {useCodePreview} from "@/contexts/code-preview-context";
import CodePreviewPanel from "@/components/markdown-viewer/code-preview-panel";
import {Artifact} from "./artifact";
import {useDataStream} from "./data-stream-provider";
import {Messages} from "./messages";
import {MultimodalInput} from "./multimodal-input";
import {getChatHistoryPaginationKey} from "./sidebar-history";
import {toast} from "./toast";
import type {VisibilityType} from "./visibility-selector";

export function Chat({
  id,
  initialMessages,
  initialChatModel,
  initialVisibilityType,
  isReadonly,
  autoResume,
  initialLastContext,
}: {
  id: string;
  initialMessages: ChatMessage[];
  initialChatModel: string;
  initialVisibilityType: VisibilityType;
  isReadonly: boolean;
  autoResume: boolean;
  initialLastContext?: AppUsage;
}) {
  const { visibilityType } = useChatVisibility({
    chatId: id,
    initialVisibilityType,
  });

  const { mutate } = useSWRConfig();
  const { setDataStream } = useDataStream();

  const [input, setInput] = useState<string>("");
  const [usage, setUsage] = useState<AppUsage | undefined>(initialLastContext);
  const [currentModelId, setCurrentModelId] = useState(initialChatModel);
  const currentModelIdRef = useRef(currentModelId);

  useEffect(() => {
    currentModelIdRef.current = currentModelId;
  }, [currentModelId]);

  const {
    messages,
    setMessages,
    sendMessage,
    status,
    stop,
    regenerate,
    resumeStream,
  } = useChat<ChatMessage>({
    id,
    messages: initialMessages,
    experimental_throttle: 100,
    generateId: generateUUID,
    transport: new DefaultChatTransport({
      api: "/api/chat",
      fetch: fetchWithErrorHandlers,
      prepareSendMessagesRequest(request) {
        return {
          body: {
            id: request.id,
            message: request.messages.at(-1),
            selectedChatModel: currentModelIdRef.current,
            selectedVisibilityType: visibilityType,
            ...request.body,
          },
        };
      },
    }),
    onData: (dataPart) => {
      setDataStream((ds) => (ds ? [...ds, dataPart] : []));
      if (dataPart.type === "data-usage") {
        setUsage(dataPart.data);
      }
    },
    onFinish: () => {
      mutate(unstable_serialize(getChatHistoryPaginationKey));
    },
    onError: (error) => {
      if (error instanceof ChatSDKError) {
        toast({
          type: "error",
          description: error.message,
        });
      }
    },
  });

  const searchParams = useSearchParams();
  const query = searchParams.get("query");

  const [hasAppendedQuery, setHasAppendedQuery] = useState(false);

  useEffect(() => {
    if (query && !hasAppendedQuery) {
      sendMessage({
        role: "user" as const,
        parts: [{ type: "text", text: query }],
      });

      setHasAppendedQuery(true);
      window.history.replaceState({}, "", `/chat/${id}`);
    }
  }, [query, sendMessage, hasAppendedQuery, id]);

  const { data: votes } = useSWR<Vote[]>(
    messages.length >= 2 ? `/api/vote?chatId=${id}` : null,
    fetcher
  );

  const [attachments, setAttachments] = useState<Attachment[]>([]);
  const isArtifactVisible = useArtifactSelector((state) => state.isVisible);
  const { state: codePreviewState, closePreview: closeCodePreview } = useCodePreview();

  useAutoResume({
    autoResume,
    initialMessages,
    resumeStream,
    setMessages,
  });

  // 如果代码预览面板打开，使用分屏布局
  if (codePreviewState.visible && codePreviewState.codeType) {
    return (
      <>
        <div className="overscroll-behavior-contain flex h-dvh min-w-0 touch-pan-y flex-col bg-background">
          <ChatHeader
            chatId={id}
            isReadonly={isReadonly}
            selectedVisibilityType={initialVisibilityType}
          />

          <PanelGroup direction="horizontal" className="flex-1 min-h-0">
            {/* 左侧：聊天内容 */}
            <Panel defaultSize={50} minSize={30} className="flex flex-col min-w-0">
              <Messages
                chatId={id}
                isArtifactVisible={isArtifactVisible}
                isReadonly={isReadonly}
                messages={messages}
                regenerate={regenerate}
                selectedModelId={initialChatModel}
                setMessages={setMessages}
                status={status}
                votes={votes}
              />

              <div className="sticky bottom-0 z-1 mx-auto flex w-full max-w-4xl gap-2 border-t-0 bg-background px-2 pb-3 md:px-4 md:pb-4">
                {!isReadonly && (
                  <MultimodalInput
                    attachments={attachments}
                    chatId={id}
                    input={input}
                    messages={messages}
                    onModelChange={setCurrentModelId}
                    selectedModelId={currentModelId}
                    selectedVisibilityType={visibilityType}
                    sendMessage={sendMessage}
                    setAttachments={setAttachments}
                    setInput={setInput}
                    setMessages={setMessages}
                    status={status}
                    stop={stop}
                    usage={usage}
                  />
                )}
              </div>
            </Panel>

            {/* 分割线 */}
            <PanelResizeHandle className="w-px bg-border relative group cursor-col-resize">
              <div className="absolute inset-y-0 left-1/2 -translate-x-1/2 w-6 flex items-center justify-center">
                <div className="w-1.5 h-12 bg-muted-foreground/30 rounded-full group-hover:bg-primary/40 transition-all duration-200 shadow-sm group-hover:shadow-md" />
              </div>
            </PanelResizeHandle>

            {/* 右侧：代码预览面板 */}
            <Panel defaultSize={50} minSize={30} className="flex flex-col min-w-0 border-l border-border">
              <CodePreviewPanel
                visible={codePreviewState.visible}
                onClose={closeCodePreview}
                code={codePreviewState.code}
                language={codePreviewState.language}
                codeType={codePreviewState.codeType}
                title="代码预览"
                layoutMode="split"
              />
            </Panel>
          </PanelGroup>
        </div>

        <Artifact
          attachments={attachments}
          chatId={id}
          input={input}
          isReadonly={isReadonly}
          messages={messages}
          regenerate={regenerate}
          selectedModelId={currentModelId}
          selectedVisibilityType={visibilityType}
          sendMessage={sendMessage}
          setAttachments={setAttachments}
          setInput={setInput}
          setMessages={setMessages}
          status={status}
          stop={stop}
          votes={votes}
        />
      </>
    );
  }

  // 默认布局（非分屏模式）
  return (
    <>
      <div className="overscroll-behavior-contain flex h-dvh min-w-0 touch-pan-y flex-col bg-background">
        <ChatHeader
          chatId={id}
          isReadonly={isReadonly}
          selectedVisibilityType={initialVisibilityType}
        />

        <Messages
          chatId={id}
          isArtifactVisible={isArtifactVisible}
          isReadonly={isReadonly}
          messages={messages}
          regenerate={regenerate}
          selectedModelId={initialChatModel}
          setMessages={setMessages}
          status={status}
          votes={votes}
        />

        <div className="sticky bottom-0 z-1 mx-auto flex w-full max-w-4xl gap-2 border-t-0 bg-background px-2 pb-3 md:px-4 md:pb-4">
          {!isReadonly && (
            <MultimodalInput
              attachments={attachments}
              chatId={id}
              input={input}
              messages={messages}
              onModelChange={setCurrentModelId}
              selectedModelId={currentModelId}
              selectedVisibilityType={visibilityType}
              sendMessage={sendMessage}
              setAttachments={setAttachments}
              setInput={setInput}
              setMessages={setMessages}
              status={status}
              stop={stop}
              usage={usage}
            />
          )}
        </div>
      </div>

      <Artifact
        attachments={attachments}
        chatId={id}
        input={input}
        isReadonly={isReadonly}
        messages={messages}
        regenerate={regenerate}
        selectedModelId={currentModelId}
        selectedVisibilityType={visibilityType}
        sendMessage={sendMessage}
        setAttachments={setAttachments}
        setInput={setInput}
        setMessages={setMessages}
        status={status}
        stop={stop}
        votes={votes}
      />

    </>
  );
}
