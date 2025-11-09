import {expect, test} from "../fixtures";
import {ChatPage} from "../pages/chat";

test.describe("聊天活动", () => {
  let chatPage: ChatPage;

  test.beforeEach(async ({ page }) => {
    chatPage = new ChatPage(page);
    await chatPage.createNewChat();
  });

  test("发送用户消息并接收响应", async () => {
    await chatPage.sendUserMessage("草为什么是绿色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toContain("它就是绿色的！");
  });

  test("重定向到 /chat/:id  after 提交消息", async () => {
    await chatPage.sendUserMessage("草为什么是绿色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toContain("它就是绿色的！");
    await chatPage.hasChatIdInUrl();
  });

  test("发送用户消息从建议", async () => {
    await chatPage.sendUserMessageFromSuggestion();
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toContain(
      "使用 Next.js，你可以快速发布！"
    );
  });

  test("切换发送/停止按钮基于活动", async () => {
    await expect(chatPage.sendButton).toBeVisible();
    await expect(chatPage.sendButton).toBeDisabled();

    await chatPage.sendUserMessage("草为什么是绿色的？");

    await expect(chatPage.sendButton).not.toBeVisible();
    await expect(chatPage.stopButton).toBeVisible();

    await chatPage.isGenerationComplete();

    await expect(chatPage.stopButton).not.toBeVisible();
    await expect(chatPage.sendButton).toBeVisible();
  });

  test("停止生成 during 提交", async () => {
    await chatPage.sendUserMessage("草为什么是绿色的？");
    await expect(chatPage.stopButton).toBeVisible();
    await chatPage.stopButton.click();
    await expect(chatPage.sendButton).toBeVisible();
  });

  test("编辑用户消息 and 重新提交", async () => {
    await chatPage.sendUserMessage("草为什么是绿色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toContain("它就是绿色的！");

    const userMessage = await chatPage.getRecentUserMessage();
    await userMessage.edit("天空为什么是蓝色的？");

    await chatPage.isGenerationComplete();

    const updatedAssistantMessage = await chatPage.getRecentAssistantMessage();
    expect(updatedAssistantMessage?.content).toContain("它就是蓝色的！");
  });

  test("隐藏建议操作 after 发送消息", async () => {
    await chatPage.isElementVisible("suggested-actions");
    await chatPage.sendUserMessageFromSuggestion();
    await chatPage.isElementNotVisible("suggested-actions");
  });

  test("上传文件并发送图片附件 with 消息", async () => {
    await chatPage.addImageAttachment();

    await chatPage.isElementVisible("attachments-preview");
    await chatPage.isElementVisible("input-attachment-loader");
    await chatPage.isElementNotVisible("input-attachment-loader");

    await chatPage.sendUserMessage("谁画了这幅画？");

    const userMessage = await chatPage.getRecentUserMessage();
    expect(userMessage.attachments).toHaveLength(1);

    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toBe("这幅画是莫奈画的！");
  });

  test("调用天气工具", async () => {
    await chatPage.sendUserMessage("旧金山今天的天气怎么样？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();

    expect(assistantMessage?.content).toBe(
      "The current temperature in San Francisco is 17°C."
    );
  });

  test("点赞消息", async () => {
    await chatPage.sendUserMessage("天空为什么是蓝色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    await assistantMessage?.upvote();
    await chatPage.isVoteComplete();
  });

  test("踩消息", async () => {
    await chatPage.sendUserMessage("天空为什么是蓝色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    await assistantMessage?.downvote();
    await chatPage.isVoteComplete();
  });

  test("更新投票", async () => {
    await chatPage.sendUserMessage("天空为什么是蓝色的？");
    await chatPage.isGenerationComplete();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    await assistantMessage?.upvote();
    await chatPage.isVoteComplete();

    await assistantMessage?.downvote();
    await chatPage.isVoteComplete();
  });

  test("从 url query 创建消息", async ({ page }) => {
    await page.goto("/?query=天空为什么是蓝色的？");

    await chatPage.isGenerationComplete();

    const userMessage = await chatPage.getRecentUserMessage();
    expect(userMessage?.content).toBe("天空为什么是蓝色的？");

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toContain("它就是蓝色的！");
  });

  test("自动滚动到底部 after 提交新消息", async () => {
    test.fixme();
    await chatPage.sendMultipleMessages(5, (i) => `filling message #${i}`);
    await chatPage.waitForScrollToBottom();
  });

  test("滚动按钮出现 when 用户滚动 up, 点击隐藏", async () => {
    test.fixme();
    await chatPage.sendMultipleMessages(5, (i) => `filling message #${i}`);
    await expect(chatPage.scrollToBottomButton).not.toBeVisible();

    await chatPage.scrollToTop();
    await expect(chatPage.scrollToBottomButton).toBeVisible();

    await chatPage.scrollToBottomButton.click();
    await chatPage.waitForScrollToBottom();
    await expect(chatPage.scrollToBottomButton).not.toBeVisible();
  });
});
