import {expect, test} from "../fixtures";
import {ChatPage} from "../pages/chat";

test.describe("聊天活动 with 推理", () => {
  let chatPage: ChatPage;

  test.beforeEach(async ({ curieContext }) => {
    chatPage = new ChatPage(curieContext.page);
    await chatPage.createNewChat();
  });

  test("Curie 可以发送消息 and 生成响应 with 推理", async () => {
    await chatPage.sendUserMessage("天空为什么是蓝色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toBe("它就是蓝色的！");

    expect(assistantMessage?.reasoning).toBe(
      "天空为什么是蓝色的？因为瑞利散射！"
    );
  });

  test("Curie 可以切换推理可见性", async () => {
    await chatPage.sendUserMessage("天空为什么是蓝色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    const reasoningElement =
      assistantMessage?.element.getByTestId("message-reasoning");
    expect(reasoningElement).toBeVisible();

    await assistantMessage?.toggleReasoningVisibility();
    await expect(reasoningElement).not.toBeVisible();

    await assistantMessage?.toggleReasoningVisibility();
    await expect(reasoningElement).toBeVisible();
  });

  test("Curie 可以编辑消息 and 重新提交", async () => {
    await chatPage.sendUserMessage("天空为什么是蓝色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    const reasoningElement =
      assistantMessage.element.getByTestId("message-reasoning");
    expect(reasoningElement).toBeVisible();

    const userMessage = await chatPage.getRecentUserMessage();

    const generationCompletePromise = chatPage.isGenerationComplete();
    await userMessage.edit("草为什么是绿色的？");
    await generationCompletePromise;

    const updatedAssistantMessage = await chatPage.getRecentAssistantMessage();

    expect(updatedAssistantMessage?.content).toBe("它就是绿色的！");

    expect(updatedAssistantMessage?.reasoning).toBe(
      "草为什么是绿色的？因为叶绿素吸收！"
    );
  });
});
