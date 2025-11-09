import type {ModelMessage} from "ai";

export const TEST_PROMPTS: Record<string, ModelMessage> = {
  USER_SKY: {
    role: "user",
    content: [{ type: "text", text: "天空为什么是蓝色的？" }],
  },
  USER_GRASS: {
    role: "user",
    content: [{ type: "text", text: "草为什么是绿色的？" }],
  },
  USER_THANKS: {
    role: "user",
    content: [{ type: "text", text: "谢谢！" }],
  },
  USER_NEXTJS: {
    role: "user",
    content: [
      { type: "text", text: "使用 Next.js 有哪些优势？" },
    ],
  },
  USER_IMAGE_ATTACHMENT: {
    role: "user",
    content: [
      {
        type: "file",
        mediaType: "...",
        data: "...",
      },
      {
        type: "text",
        text: "谁画了这幅画？",
      },
    ],
  },
  USER_TEXT_ARTIFACT: {
    role: "user",
    content: [
      {
        type: "text",
        text: "帮我写一篇关于硅谷的论文",
      },
    ],
  },
  CREATE_DOCUMENT_TEXT_CALL: {
    role: "user",
    content: [
      {
        type: "text",
        text: "硅谷论文",
      },
    ],
  },
  CREATE_DOCUMENT_TEXT_RESULT: {
    role: "tool",
    content: [
      {
        type: "tool-result",
        toolCallId: "call_123",
        toolName: "createDocument",
        output: {
          type: "json",
          value: {
            id: "3ca386a4-40c6-4630-8ed1-84cbd46cc7eb",
            title: "硅谷论文",
            kind: "text",
            content: "一个文档被创建并现在对用户可见。",
          },
        },
      },
    ],
  },
  GET_WEATHER_CALL: {
    role: "user",
    content: [
      {
        type: "text",
        text: "旧金山今天的天气怎么样？",
      },
    ],
  },
  GET_WEATHER_RESULT: {
    role: "tool",
    content: [
      {
        type: "tool-result",
        toolCallId: "call_456",
        toolName: "getWeather",
        output: {
          type: "json",
          value: {
            latitude: 37.763_283,
            longitude: -122.412_86,
            generationtime_ms: 0.064_492_225_646_972_66,
            utc_offset_seconds: -25_200,
            timezone: "America/Los_Angeles",
            timezone_abbreviation: "GMT-7",
            elevation: 18,
            current_units: {
              time: "iso8601",
              interval: "seconds",
              temperature_2m: "°C",
            },
            current: {
              time: "2025-03-10T14:00",
              interval: 900,
              temperature_2m: 17,
            },
            daily_units: {
              time: "iso8601",
              sunrise: "iso8601",
              sunset: "iso8601",
            },
            daily: {
              time: [
                "2025-03-10",
                "2025-03-11",
                "2025-03-12",
                "2025-03-13",
                "2025-03-14",
                "2025-03-15",
                "2025-03-16",
              ],
              sunrise: [
                "2025-03-10T07:27",
                "2025-03-11T07:25",
                "2025-03-12T07:24",
                "2025-03-13T07:22",
                "2025-03-14T07:21",
                "2025-03-15T07:19",
                "2025-03-16T07:18",
              ],
              sunset: [
                "2025-03-10T19:12",
                "2025-03-11T19:13",
                "2025-03-12T19:14",
                "2025-03-13T19:15",
                "2025-03-14T19:16",
                "2025-03-15T19:17",
                "2025-03-16T19:17",
              ],
            },
          },
        },
      },
    ],
  },
};
