import {expect, test} from "../fixtures";
import {ArtifactPage} from "../pages/artifact";
import {ChatPage} from "../pages/chat";

test.describe("文档活动", () => {
  let chatPage: ChatPage;
  let artifactPage: ArtifactPage;

  test.beforeEach(async ({ page }) => {
    chatPage = new ChatPage(page);
    artifactPage = new ArtifactPage(page);

    await chatPage.createNewChat();
  });

  test("创建文本文档", async () => {
    test.fixme();
    await chatPage.createNewChat();

    await chatPage.sendUserMessage(
      "帮我写一篇关于硅谷的论文"
    );
    await artifactPage.isGenerationComplete();

    expect(artifactPage.artifact).toBeVisible();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toBe(
      "一个文档被创建并现在对用户可见。"
    );

    await chatPage.hasChatIdInUrl();
  });

  test("切换文档可见性", async () => {
    test.fixme();
    await chatPage.createNewChat();

    await chatPage.sendUserMessage(
      "帮我写一篇关于硅谷的论文"
    );
    await artifactPage.isGenerationComplete();

    expect(artifactPage.artifact).toBeVisible();

    const assistantMessage = await chatPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toBe(
      "一个文档被创建并现在对用户可见。"
    );

    await artifactPage.closeArtifact();
    await chatPage.isElementNotVisible("artifact");
  });

  test("发送后续消息", async () => {
    test.fixme();
    await chatPage.createNewChat();

    await chatPage.sendUserMessage(
      "帮我写一篇关于硅谷的论文"
    );
    await artifactPage.isGenerationComplete();

    expect(artifactPage.artifact).toBeVisible();

    const assistantMessage = await artifactPage.getRecentAssistantMessage();
    expect(assistantMessage?.content).toBe(
      "一个文档被创建并现在对用户可见。"
    );

    await artifactPage.sendUserMessage("Thanks!");
    await artifactPage.isGenerationComplete();

    const secondAssistantMessage = await chatPage.getRecentAssistantMessage();
    expect(secondAssistantMessage?.content).toBe("不客气！");
  });
});
