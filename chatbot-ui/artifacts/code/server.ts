import {streamText} from "ai";
import {codePrompt, updateDocumentPrompt} from "@/lib/ai/prompts";
import {myProvider} from "@/lib/ai/providers";
import {createDocumentHandler} from "@/lib/artifacts/server";

/**
 * 从 Markdown 代码块中提取纯代码
 * 移除 ```language 和 ``` 标记
 */
function extractCodeFromMarkdown(content: string): string {
  // 匹配 markdown 代码块格式: ```language\ncode\n```
  const codeBlockRegex = /^```[\w]*\n([\s\S]*?)\n```$/;
  const match = content.trim().match(codeBlockRegex);
  
  if (match) {
    return match[1];
  }
  
  // 如果没有匹配到完整的代码块，但以 ``` 开头，尝试提取
  if (content.trim().startsWith("```")) {
    const lines = content.trim().split("\n");
    // 移除第一行（```language）和最后一行（```）
    if (lines.length > 2 && lines[lines.length - 1].trim() === "```") {
      return lines.slice(1, -1).join("\n");
    }
    // 如果还在流式传输中，可能最后的 ``` 还没到
    if (lines.length > 1) {
      return lines.slice(1).join("\n");
    }
  }
  
  // 如果不是 markdown 格式，直接返回原内容
  return content;
}

export const codeDocumentHandler = createDocumentHandler<"code">({
  kind: "code",
  onCreateDocument: async ({ title, dataStream }) => {
    let draftContent = "";

    const { textStream } = streamText({
      model: myProvider.languageModel("artifact-model"),
      system: codePrompt,
      prompt: title,
    });

    for await (const chunk of textStream) {
      draftContent += chunk;
      
      // 实时提取代码（用于流式显示）
      const extractedCode = extractCodeFromMarkdown(draftContent);
      
      dataStream.write({
        type: "data-codeDelta",
        data: extractedCode,
        transient: true,
      });
    }

    // 最终提取纯代码
    const finalCode = extractCodeFromMarkdown(draftContent);

    // 调试日志：检查生成的代码内容
    console.log("===== CODE DOCUMENT HANDLER =====");
    console.log("Title:", title);
    console.log("Raw content (first 200 chars):", draftContent.slice(0, 200));
    console.log("Has markdown code block:", draftContent.includes("```"));
    console.log("Extracted code (first 200 chars):", finalCode.slice(0, 200));
    console.log("================================");

    return finalCode;
  },
  onUpdateDocument: async ({ document, description, dataStream }) => {
    let draftContent = "";

    const { textStream } = streamText({
      model: myProvider.languageModel("artifact-model"),
      system: updateDocumentPrompt(document.content, "code"),
      prompt: description,
    });

    for await (const chunk of textStream) {
      draftContent += chunk;
      
      // 实时提取代码（用于流式显示）
      const extractedCode = extractCodeFromMarkdown(draftContent);
      
      dataStream.write({
        type: "data-codeDelta",
        data: extractedCode,
        transient: true,
      });
    }

    // 最终提取纯代码
    const finalCode = extractCodeFromMarkdown(draftContent);
    return finalCode;
  },
});
