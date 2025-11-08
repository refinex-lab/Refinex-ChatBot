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
    name: "DeepSeek Chat",
    description: "DeepSeek 通用对话模型，支持文本和视觉处理",
  },
  {
    id: "chat-model-reasoning",
    name: "DeepSeek Reasoner",
    description: "DeepSeek 推理模型，使用链式思维解决复杂问题",
  },
];
