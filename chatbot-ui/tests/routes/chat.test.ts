import {getMessageByErrorCode} from "@/lib/errors";
import {generateUUID} from "@/lib/utils";
import {expect, test} from "../fixtures";
import {TEST_PROMPTS} from "../prompts/routes";

const chatIdsCreatedByAda: string[] = [];

// 辅助函数，用于规范化流数据以进行比较
function normalizeStreamData(lines: string[]): string[] {
  return lines.map((line) => {
    if (line.startsWith("data: ")) {
      try {
        const data = JSON.parse(line.slice(6)); // 移除 'data: ' 前缀
        if (data.id) {
          // 替换动态 id 为静态 id 用于比较
          return `data: ${JSON.stringify({ ...data, id: "STATIC_ID" })}`;
        }
        return line;
      } catch {
        return line; // 如果它不是有效的 JSON，则返回原样
      }
    }
    return line;
  });
}

test.describe
  .serial("/api/chat", () => {
    test("Ada 无法在没有请求体的情况下调用聊天生成", async ({
      adaContext,
    }) => {
      const response = await adaContext.request.post("/api/chat", {
        data: JSON.stringify({}),
      });
      expect(response.status()).toBe(400);

      const { code, message } = await response.json();
      expect(code).toEqual("bad_request:api");
      expect(message).toEqual(getMessageByErrorCode("bad_request:api"));
    });

    test("Ada 可以调用聊天生成", async ({ adaContext }) => {
      const chatId = generateUUID();

      const response = await adaContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: TEST_PROMPTS.SKY.MESSAGE,
          selectedChatModel: "chat-model",
          selectedVisibilityType: "private",
        },
      });
      expect(response.status()).toBe(200);

      const text = await response.text();
      const lines = text.split("\n");

      const [_, ...rest] = lines;
      const actualNormalized = normalizeStreamData(rest.filter(Boolean));
      const expectedNormalized = normalizeStreamData(
        TEST_PROMPTS.SKY.OUTPUT_STREAM
      );

      expect(actualNormalized).toEqual(expectedNormalized);

      chatIdsCreatedByAda.push(chatId);
    });

    test("Babbage 无法附加消息到 Ada 的聊天", async ({
      babbageContext,
    }) => {
      const [chatId] = chatIdsCreatedByAda;

      const response = await babbageContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: TEST_PROMPTS.GRASS.MESSAGE,
          selectedChatModel: "chat-model",
          selectedVisibilityType: "private",
        },
      });
      expect(response.status()).toBe(403);

      const { code, message } = await response.json();
      expect(code).toEqual("forbidden:chat");
      expect(message).toEqual(getMessageByErrorCode("forbidden:chat"));
    });

    test("Babbage 无法删除 Ada 的聊天", async ({ babbageContext }) => {
      const [chatId] = chatIdsCreatedByAda;

      const response = await babbageContext.request.delete(
        `/api/chat?id=${chatId}`
      );
      expect(response.status()).toBe(403);

      const { code, message } = await response.json();
      expect(code).toEqual("forbidden:chat");
      expect(message).toEqual(getMessageByErrorCode("forbidden:chat"));
    });

    test("Ada 可以删除自己的聊天", async ({ adaContext }) => {
      const [chatId] = chatIdsCreatedByAda;

      const response = await adaContext.request.delete(
        `/api/chat?id=${chatId}`
      );
      expect(response.status()).toBe(200);

      const deletedChat = await response.json();
      expect(deletedChat).toMatchObject({ id: chatId });
    });

    test("Ada 无法恢复不存在的聊天的流", async ({
      adaContext,
    }) => {
      const response = await adaContext.request.get(
        `/api/chat/${generateUUID()}/stream`
      );
      expect(response.status()).toBe(404);
    });

    test("Ada 可以恢复聊天生成", async ({ adaContext }) => {
      const chatId = generateUUID();

      const firstRequest = adaContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: {
            id: generateUUID(),
            role: "user",
            content: "帮我写一篇关于硅谷的论文",
            parts: [
              {
                type: "text",
                text: "帮我写一篇关于硅谷的论文",
              },
            ],
            createdAt: new Date().toISOString(),
          },
          selectedChatModel: "chat-model",
          selectedVisibilityType: "private",
        },
      });

      await new Promise((resolve) => setTimeout(resolve, 1000));

      const secondRequest = adaContext.request.get(
        `/api/chat/${chatId}/stream`
      );

      const [firstResponse, secondResponse] = await Promise.all([
        firstRequest,
        secondRequest,
      ]);

      const [firstStatusCode, secondStatusCode] = await Promise.all([
        firstResponse.status(),
        secondResponse.status(),
      ]);

      expect(firstStatusCode).toBe(200);
      expect(secondStatusCode).toBe(200);

      const [firstResponseBody, secondResponseBody] = await Promise.all([
        await firstResponse.body(),
        await secondResponse.body(),
      ]);

      expect(firstResponseBody.toString()).toEqual(
        secondResponseBody.toString()
      );
    });

    test("Ada 可以恢复在请求期间结束的聊天生成", async ({
      adaContext,
    }) => {
      const chatId = generateUUID();

      const firstRequest = await adaContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: {
            id: generateUUID(),
            role: "user",
            content: "帮我写一篇关于硅谷的论文",
            parts: [
              {
                type: "text",
                text: "Help me write an essay about Silicon Valley",
              },
            ],
            createdAt: new Date().toISOString(),
          },
          selectedChatModel: "chat-model",
          selectedVisibilityType: "private",
        },
      });

      const secondRequest = adaContext.request.get(
        `/api/chat/${chatId}/stream`
      );

      const [firstResponse, secondResponse] = await Promise.all([
        firstRequest,
        secondRequest,
      ]);

      const [firstStatusCode, secondStatusCode] = await Promise.all([
        firstResponse.status(),
        secondResponse.status(),
      ]);

      expect(firstStatusCode).toBe(200);
      expect(secondStatusCode).toBe(200);

      const [, secondResponseContent] = await Promise.all([
        firstResponse.text(),
        secondResponse.text(),
      ]);

      expect(secondResponseContent).toContain("appendMessage");
    });

    test("Ada 无法恢复已结束的聊天生成", async ({
      adaContext,
    }) => {
      const chatId = generateUUID();

      const firstResponse = await adaContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: {
            id: generateUUID(),
            role: "user",
            content: "帮我写一篇关于硅谷的论文",
            parts: [
              {
                type: "text",
                text: "帮我写一篇关于硅谷的论文",
              },
            ],
            createdAt: new Date().toISOString(),
          },
          selectedChatModel: "chat-model",
          selectedVisibilityType: "private",
        },
      });

      const firstStatusCode = firstResponse.status();
      expect(firstStatusCode).toBe(200);

      await firstResponse.text();
      await new Promise((resolve) => setTimeout(resolve, 15 * 1000));
      await new Promise((resolve) => setTimeout(resolve, 15_000));
      const secondResponse = await adaContext.request.get(
        `/api/chat/${chatId}/stream`
      );

      const secondStatusCode = secondResponse.status();
      expect(secondStatusCode).toBe(200);

      const secondResponseContent = await secondResponse.text();
      expect(secondResponseContent).toEqual("");
    });

    test("Babbage 无法恢复属于 Ada 的私有聊天生成", async ({
      adaContext,
      babbageContext,
    }) => {
      const chatId = generateUUID();

      const firstRequest = adaContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: {
            id: generateUUID(),
            role: "user",
            content: "帮我写一篇关于硅谷的论文",
            parts: [
              {
                type: "text",
                text: "帮我写一篇关于硅谷的论文",
              },
            ],
            createdAt: new Date().toISOString(),
          },
          selectedChatModel: "chat-model",
          selectedVisibilityType: "private",
        },
      });

      await new Promise((resolve) => setTimeout(resolve, 1000));

      const secondRequest = babbageContext.request.get(
        `/api/chat/${chatId}/stream`
      );

      const [firstResponse, secondResponse] = await Promise.all([
        firstRequest,
        secondRequest,
      ]);

      const [firstStatusCode, secondStatusCode] = await Promise.all([
        firstResponse.status(),
        secondResponse.status(),
      ]);

      expect(firstStatusCode).toBe(200);
      expect(secondStatusCode).toBe(403);
    });

    test("Babbage 可以恢复属于 Ada 的公共聊天生成", async ({
      adaContext,
      babbageContext,
    }) => {
      test.fixme();
      const chatId = generateUUID();

      const firstRequest = adaContext.request.post("/api/chat", {
        data: {
          id: chatId,
          message: {
            id: generateUUID(),
            role: "user",
            content: "帮我写一篇关于硅谷的论文",
            parts: [
              {
                type: "text",
                text: "帮我写一篇关于硅谷的论文",
              },
            ],
            createdAt: new Date().toISOString(),
          },
          selectedChatModel: "chat-model",
          selectedVisibilityType: "public",
        },
      });

      await new Promise((resolve) => setTimeout(resolve, 10 * 1000));

      const secondRequest = babbageContext.request.get(
        `/api/chat/${chatId}/stream`
      );

      const [firstResponse, secondResponse] = await Promise.all([
        firstRequest,
        secondRequest,
      ]);

      const [firstStatusCode, secondStatusCode] = await Promise.all([
        firstResponse.status(),
        secondResponse.status(),
      ]);

      expect(firstStatusCode).toBe(200);
      expect(secondStatusCode).toBe(200);

      const [firstResponseContent, secondResponseContent] = await Promise.all([
        firstResponse.text(),
        secondResponse.text(),
      ]);

      expect(firstResponseContent).toEqual(secondResponseContent);
    });
  });
