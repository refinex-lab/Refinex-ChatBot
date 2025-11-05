/**
 * AI 聊天模型
 */
export const DEFAULT_CHAT_MODEL: string = "chat-model";

export type ChatModel = {
  id: string;
  name: string;
  description: string;
};

export const chatModels: ChatModel[] = [
  {
    id: "chat-model",
    name: "Grok 视觉模型",
    description: "高级多模态模型，具有视觉和文本能力",
  },
  {
    id: "chat-model-reasoning",
    name: "Grok 推理模型",
    description:
      "使用高级链式思维推理解决复杂问题",
  },
];
