<a href="https://chat.vercel.ai/">
  <img alt="基于 Next.js 14 和 App Router 的 AI 聊天机器人。" src="app/(chat)/opengraph-image.png">
  <h1 align="center">Chat SDK</h1>
</a>

<p align="center">
    Chat SDK 是一个免费的开源模板，基于 Next.js 和 AI SDK 构建，帮助您快速构建强大的聊天机器人应用程序。
</p>

<p align="center">
  <a href="https://chat-sdk.dev"><strong>阅读文档</strong></a> ·
  <a href="#features"><strong>功能特性</strong></a> ·
  <a href="#model-providers"><strong>模型提供商</strong></a> ·
  <a href="#deploy-your-own"><strong>部署您自己的版本</strong></a> ·
  <a href="#running-locally"><strong>本地运行</strong></a>
</p>
<br/>

## 功能特性

- [Next.js](https://nextjs.org) App Router
  - 先进的路由系统，实现无缝导航和高性能
  - React 服务器组件 (RSCs) 和服务器操作，用于服务器端渲染和提升性能
- [AI SDK](https://ai-sdk.dev/docs/introduction)
  - 统一的 API，用于通过 LLM 生成文本、结构化对象和工具调用
  - 用于构建动态聊天和生成式用户界面的 Hooks
  - 支持 xAI（默认）、OpenAI、Fireworks 和其他模型提供商
- [shadcn/ui](https://ui.shadcn.com)
  - 使用 [Tailwind CSS](https://tailwindcss.com) 进行样式设计
  - 来自 [Radix UI](https://radix-ui.com) 的组件原语，提供可访问性和灵活性
- 数据持久化
  - [Neon Serverless Postgres](https://vercel.com/marketplace/neon) 用于保存聊天历史和用户数据
  - [Vercel Blob](https://vercel.com/storage/blob) 用于高效的文件存储
- [Auth.js](https://authjs.dev)
  - 简单安全的身份验证

## 模型提供商

此模板使用 [Vercel AI Gateway](https://vercel.com/docs/ai-gateway) 通过统一接口访问多个 AI 模型。默认配置包括通过网关路由的 [xAI](https://x.ai) 模型（`grok-2-vision-1212`、`grok-3-mini`）。

### AI Gateway 身份验证

**对于 Vercel 部署**：身份验证通过 OIDC 令牌自动处理。

**对于非 Vercel 部署**：您需要在 `.env.local` 文件中设置 `AI_GATEWAY_API_KEY` 环境变量来提供 AI Gateway API 密钥。

通过 [AI SDK](https://ai-sdk.dev/docs/introduction)，您还可以仅用几行代码切换到直接的 LLM 提供商，如 [OpenAI](https://openai.com)、[Anthropic](https://anthropic.com)、[Cohere](https://cohere.com/) 以及[更多提供商](https://ai-sdk.dev/providers/ai-sdk-providers)。

## 部署您自己的版本

您可以一键将自己的 Next.js AI 聊天机器人版本部署到 Vercel：

[![使用 Vercel 部署](https://vercel.com/button)](https://vercel.com/templates/next.js/nextjs-ai-chatbot)

## 本地运行

您需要使用 [在 `.env.example` 中定义的](.env.example) 环境变量来运行 Next.js AI 聊天机器人。建议您使用 [Vercel 环境变量](https://vercel.com/docs/projects/environment-variables)，但仅使用 `.env` 文件也足够了。

> 注意：您不应该提交 `.env` 文件，否则会暴露密钥，允许他人控制您的各种 AI 和身份验证提供商账户的访问权限。

1. 安装 Vercel CLI：`npm i -g vercel`
2. 将本地实例与 Vercel 和 GitHub 账户关联（创建 `.vercel` 目录）：`vercel link`
3. 下载环境变量：`vercel env pull`

```bash
pnpm install
pnpm db:migrate # 设置数据库或应用最新的数据库更改
pnpm dev
```

您的应用模板现在应该在 [localhost:3000](http://localhost:3000) 上运行了。
