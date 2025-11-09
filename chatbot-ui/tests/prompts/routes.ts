import {generateUUID} from "@/lib/utils";

export const TEST_PROMPTS = {
  SKY: {
    MESSAGE: {
      id: generateUUID(),
      createdAt: new Date().toISOString(),
      role: "user",
      content: "天空为什么是蓝色的？",
      parts: [{ type: "text", text: "天空为什么是蓝色的？" }],
    },
    OUTPUT_STREAM: [
      'data: {"type":"start-step"}',
      'data: {"type":"text-start","id":"STATIC_ID"}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"It\'s "}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"just "}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"blue "}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"duh! "}',
      'data: {"type":"text-end","id":"STATIC_ID"}',
      'data: {"type":"finish-step"}',
      'data: {"type":"finish"}',
      "data: [DONE]",
    ],
  },
  GRASS: {
    MESSAGE: {
      id: generateUUID(),
      createdAt: new Date().toISOString(),
      role: "user",
      content: "草为什么是绿色的？",
      parts: [{ type: "text", text: "草为什么是绿色的？" }],
    },
    OUTPUT_STREAM: [
      'data: {"type":"start-step"}',
      'data: {"type":"text-start","id":"STATIC_ID"}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"It\'s "}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"just "}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"green "}',
      'data: {"type":"text-delta","id":"STATIC_ID","delta":"duh! "}',
      'data: {"type":"text-end","id":"STATIC_ID"}',
      'data: {"type":"finish-step"}',
      'data: {"type":"finish"}',
      "data: [DONE]",
    ],
  },
};
