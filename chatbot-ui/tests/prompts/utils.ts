import type {LanguageModelV2StreamPart} from "@ai-sdk/provider";
import {generateId, type ModelMessage} from "ai";
import {TEST_PROMPTS} from "./basic";

export function compareMessages(
  firstMessage: ModelMessage,
  secondMessage: ModelMessage
): boolean {
  if (firstMessage.role !== secondMessage.role) {
    return false;
  }

  if (
    !Array.isArray(firstMessage.content) ||
    !Array.isArray(secondMessage.content)
  ) {
    return false;
  }

  if (firstMessage.content.length !== secondMessage.content.length) {
    return false;
  }

  for (let i = 0; i < firstMessage.content.length; i++) {
    const item1 = firstMessage.content[i];
    const item2 = secondMessage.content[i];

    if (item1.type !== item2.type) {
      return false;
    }

    if (item1.type === "file" && item2.type === "file") {
      // if (item1.image.toString() !== item2.image.toString()) return false;
      // if (item1.mimeType !== item2.mimeType) return false;
    } else if (item1.type === "text" && item2.type === "text") {
      if (item1.text !== item2.text) {
        return false;
      }
    } else if (item1.type === "tool-result" && item2.type === "tool-result") {
      if (item1.toolCallId !== item2.toolCallId) {
        return false;
      }
    } else {
      return false;
    }
  }

  return true;
}

const textToDeltas = (text: string): LanguageModelV2StreamPart[] => {
  const id = generateId();

  const deltas = text.split(" ").map((char) => ({
    id,
    type: "text-delta" as const,
    delta: `${char} `,
  }));

  return [{ id, type: "text-start" }, ...deltas, { id, type: "text-end" }];
};

const reasoningToDeltas = (text: string): LanguageModelV2StreamPart[] => {
  const id = generateId();

  const deltas = text.split(" ").map((char) => ({
    id,
    type: "reasoning-delta" as const,
    delta: `${char} `,
  }));

  return [
    { id, type: "reasoning-start" },
    ...deltas,
    { id, type: "reasoning-end" },
  ];
};

export const getResponseChunksByPrompt = (
  prompt: ModelMessage[],
  isReasoningEnabled = false
): LanguageModelV2StreamPart[] => {
  const recentMessage = prompt.at(-1);

  if (!recentMessage) {
    throw new Error("No recent message found!");
  }

  if (isReasoningEnabled) {
    if (compareMessages(recentMessage, TEST_PROMPTS.USER_SKY)) {
      return [
        ...reasoningToDeltas("天空为什么是蓝色的？因为瑞利散射！"),
        ...textToDeltas("It's just blue duh!"),
        {
          type: "finish",
          finishReason: "stop",
          usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
        },
      ];
    }

    if (compareMessages(recentMessage, TEST_PROMPTS.USER_GRASS)) {
      return [
        ...reasoningToDeltas(
          "草为什么是绿色的？因为叶绿素吸收！"
        ),
        ...textToDeltas("It's just green duh!"),
        {
          type: "finish",
          finishReason: "stop",
          usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
        },
      ];
    }
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.USER_THANKS)) {
    return [
      ...textToDeltas("不客气！"),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.USER_GRASS)) {
    return [
      ...textToDeltas("它就是绿色的！"),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.USER_SKY)) {
    return [
      ...textToDeltas("它就是蓝色的！"),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.USER_NEXTJS)) {
    return [
      ...textToDeltas("使用 Next.js，你可以快速发布！"),

      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.USER_IMAGE_ATTACHMENT)) {
    return [
      ...textToDeltas("这幅画是莫奈画的！"),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.USER_TEXT_ARTIFACT)) {
    const toolCallId = generateId();

    return [
      {
        id: toolCallId,
        type: "tool-input-start",
        toolName: "createDocument",
      },
      {
        id: toolCallId,
        type: "tool-input-delta",
        delta: JSON.stringify({
          title: "硅谷论文",
          kind: "text",
        }),
      },
      {
        id: toolCallId,
        type: "tool-input-end",
      },
      {
        toolCallId,
        type: "tool-result",
        toolName: "createDocument",
        result: {
          id: "doc_123",
          title: "硅谷论文",
          kind: "text",
        },
      },
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.CREATE_DOCUMENT_TEXT_CALL)) {
    return [
      ...textToDeltas(`\n
# 硅谷：创新的中心

## 起源和演变

硅谷，位于旧金山湾区的南部，在20世纪末成为全球科技中心。它的转变始于1950年代，斯坦福大学鼓励其毕业生在附近创办自己的公司，形成了最早的半导体公司，从而赋予了该地区硅谷的名称。

## 创新生态系统

硅谷的独特之处在于它拥有完美的要素组合：斯坦福和伯克利等顶尖大学、充足的创业资本、勇于冒险的文化和密集的人才网络。这个生态系统一直在培养突破性的技术，从个人电脑到社交媒体平台再到人工智能。

## 挑战和批评

尽管硅谷取得了显著的成功，但它面临着严重的挑战，包括极端的收入不平等、住房负担能力危机以及对技术对社会影响的质疑。批评者认为该地区发展了一种单一文化，有时在多样性和包容性方面存在问题。

## 未来展望

随着我们继续前进，硅谷将继续自我重塑。虽然有些人预测由于远程工作趋势和来自其他科技中心的竞争，硅谷的衰落，但该地区的适应能力和创新精神表明，它将继续在塑造我们的技术未来几十年中发挥重要作用。
`),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (
    compareMessages(recentMessage, TEST_PROMPTS.CREATE_DOCUMENT_TEXT_RESULT)
  ) {
    return [
      ...textToDeltas("一个文档被创建并现在对用户可见。"),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.GET_WEATHER_CALL)) {
    return [
      {
        type: "tool-call",
        toolCallId: "call_456",
        toolName: "getWeather",
        input: JSON.stringify({ latitude: 37.7749, longitude: -122.4194 }),
      },
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  if (compareMessages(recentMessage, TEST_PROMPTS.GET_WEATHER_RESULT)) {
    return [
      ...textToDeltas("旧金山今天的天气是17°C。"),
      {
        type: "finish",
        finishReason: "stop",
        usage: { inputTokens: 3, outputTokens: 10, totalTokens: 13 },
      },
    ];
  }

  return [{ id: "6", type: "text-delta", delta: "未知的测试提示！" }];
};
