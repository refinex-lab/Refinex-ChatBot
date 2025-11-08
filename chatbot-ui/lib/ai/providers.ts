import {createOpenAI} from "@ai-sdk/openai";
import {customProvider, extractReasoningMiddleware, wrapLanguageModel,} from "ai";
import {isTestEnvironment} from "../constants";

// 创建 DeepSeek 客户端
// DeepSeek API 完全兼容 OpenAI 格式
const deepseek = createOpenAI({
  apiKey: process.env.DEEPSEEK_API_KEY,
  baseURL: "https://api.deepseek.com",
});

export const myProvider = isTestEnvironment
  ? (() => {
      const {
        artifactModel,
        chatModel,
        reasoningModel,
        titleModel,
      } = require("./models.mock");
      return customProvider({
        languageModels: {
          "chat-model": chatModel,
          "chat-model-reasoning": reasoningModel,
          "title-model": titleModel,
          "artifact-model": artifactModel,
        },
      });
    })()
  : customProvider({
      languageModels: {
        "chat-model": deepseek.chat("deepseek-chat"),
        "chat-model-reasoning": wrapLanguageModel({
          model: deepseek.chat("deepseek-reasoner"),
          middleware: extractReasoningMiddleware({ tagName: "think" }),
        }),
        "title-model": deepseek.chat("deepseek-chat"),
        "artifact-model": deepseek.chat("deepseek-chat"),
      },
    });
