# Shadcn Admin Dashboard

基于 Shadcn 和 Vite 构建的管理后台 UI。专注于响应式与可访问性设计。

![alt text](public/images/shadcn-admin.png)

[![Sponsored by Clerk](https://img.shields.io/badge/Sponsored%20by-Clerk-5b6ee1?logo=clerk)](https://go.clerk.com/GttUAaK)

我一直在工作中以及个人项目里制作各种 Dashboard UI。一直想创建一个可复用的后台 UI 组件集合，用于未来的项目；现在它终于来了。虽然我制作了一些自定义组件，但部分代码直接来源于 ShadcnUI 的示例。

> 不过，这还不是一个可直接使用的启动模板（starter）。未来可能会制作。

## 功能特性

- 明亮/深色模式
- 响应式设计
- 可访问性支持
- 内置 Sidebar 组件
- 全局搜索命令
- 10+ 页面示例
- 更多额外自定义组件
- RTL（从右到左语言）支持

<details>
<summary>自定义组件（点击展开）</summary>

本项目使用了 Shadcn UI 的组件，但其中部分组件为了更好的 RTL 支持及其它改进被进行了轻微修改。这些自定义组件与原版的 Shadcn UI 版本存在差异。

如果你希望使用 Shadcn CLI 更新组件（例如：`npx shadcn@latest add <component>`），对于未修改的组件通常是安全的。而对于下方列出的“已修改组件”，你可能需要手动合并变更，以保留项目中的自定义内容，避免 RTL 支持或其他修改被覆盖掉。

> 如果你不需要 RTL 支持，“RTL 更新组件”可以安全通过 Shadcn CLI 更新，因为这些改动主要用于 RTL 兼容性。而“修改组件”可能包含其它功能性调整，需要注意。

### 修改组件（Modified Components）

- scroll-area
- sonner
- separator

### RTL 更新组件（RTL Updated Components）

- alert-dialog
- calendar
- command
- dialog
- dropdown-menu
- select
- table
- sheet
- sidebar
- switch

**说明:**

- **修改组件**: 这些组件包含了常规更新，也可能包含 RTL 调整。
- **RTL 更新组件**: 这些组件仅针对 RTL 语言支持做了修改（如布局与定位）。
- 具体实现细节请查看 `src/components/ui/` 目录下的源文件。
- 项目中的所有其他 Shadcn UI 组件均为标准版本，可安全通过 CLI 更新。

</details>

## 技术栈（Tech Stack）

**UI:** [ShadcnUI](https://ui.shadcn.com) (TailwindCSS + RadixUI)

**构建工具:** [Vite](https://vitejs.dev/)

**路由:** [TanStack Router](https://tanstack.com/router/latest)

**类型检查:** [TypeScript](https://www.typescriptlang.org/)

**代码检查/格式化:** [ESLint](https://eslint.org/) & [Prettier](https://prettier.io/)

**图标:** [Lucide Icons](https://lucide.dev/icons/), [Tabler Icons](https://tabler.io/icons) (Brand icons only)

**认证（部分）:** [Clerk](https://go.clerk.com/GttUAaK)

## 本地运行

克隆项目：

```bash
  git clone https://github.com/satnaing/shadcn-admin.git
```

进入项目目录：

```bash
  cd shadcn-admin
```

安装依赖：

```bash
  pnpm install
```

启动本地开发服务器：

```bash
  pnpm run dev
```

## 赞助本项目 ❤️

如果你觉得这个项目对你有帮助，或在你的项目中使用了它，可以考虑[暂住我](https://github.com/sponsors/satnaing) 来支持后续的开发与维护。你也可以 [请我喝杯咖啡](https://buymeacoffee.com/satnaing) 。不用担心，每一份支持都非常宝贵。谢谢！🙏

如有任何问题或赞助相关询问，可联系：[satnaingdev@gmail.com](mailto:satnaingdev@gmail.com).

### 当前赞助商

- [Clerk](https://go.clerk.com/GttUAaK) - 现代 Web 的认证与用户管理服务

## Author

Crafted with 🤍 by [@satnaing](https://github.com/satnaing)

## License

Licensed under the [MIT License](https://choosealicense.com/licenses/mit/)
