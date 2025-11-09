/**
 * MarkdownViewer - 专业的 Markdown 预览组件
 *
 * 基于 react-markdown 实现，支持：
 * - GitHub Flavored Markdown (GFM)
 * - 代码语法高亮
 * - HTML 安全渲染
 * - Mermaid 图表渲染
 * - 自定义样式
 *
 * @author Refinex Team
 */

import React, {useMemo} from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import rehypeRaw from 'rehype-raw';
import rehypeSanitize from 'rehype-sanitize';
import {Empty} from 'antd';
import classNames from 'classnames';
import CodeBlock from './CodeBlock';
import './index.less';
import 'highlight.js/styles/github.css'; // GitHub 风格代码高亮

export interface MarkdownViewerProps {
  /** Markdown 内容 */
  content?: string;
  /** 自定义类名 */
  className?: string;
  /** 自定义样式 */
  style?: React.CSSProperties;
  /** 是否显示边框 */
  bordered?: boolean;
  /** 是否启用代码高亮 */
  enableHighlight?: boolean;
  /** 是否允许 HTML */
  allowHtml?: boolean;
  /** 是否启用 Mermaid 图表 */
  enableMermaid?: boolean;
  /** 空状态提示 */
  emptyText?: string;
}

/**
 * Markdown 预览组件
 */
const MarkdownViewer: React.FC<MarkdownViewerProps> = ({
  content,
  className,
  style,
  bordered = false,
  enableHighlight = true,
  allowHtml = false,
  enableMermaid = true,
  emptyText = '暂无内容',
}) => {
  // 计算 rehype 插件列表
  const rehypePlugins = useMemo(() => {
    const plugins: any[] = [];

    // HTML 支持（需先启用 rehypeRaw）
    if (allowHtml) {
      plugins.push(rehypeRaw);
      plugins.push(rehypeSanitize); // 安全性：清理 HTML
    }

    // 代码高亮
    if (enableHighlight) {
      plugins.push(rehypeHighlight);
    }

    return plugins;
  }, [allowHtml, enableHighlight]);

  // 空状态处理
  if (!content || content.trim() === '') {
    return (
      <div className={classNames('markdown-viewer-empty', className)} style={style}>
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={emptyText}
          style={{ padding: '40px 0' }}
        />
      </div>
    );
  }

  return (
    <div
      className={classNames(
        'markdown-viewer',
        {
          'markdown-viewer-bordered': bordered,
        },
        className,
      )}
      style={style}
    >
      <ReactMarkdown
        remarkPlugins={[remarkGfm]} // GitHub Flavored Markdown
        rehypePlugins={rehypePlugins}
        // 自定义组件映射
        components={{
          // 表格增强
          table: ({ children }) => (
            <div className="markdown-table-wrapper">
              <table>{children}</table>
            </div>
          ),
          // 链接增强（新窗口打开）
          a: ({ href, children, ...props }) => (
            <a
              href={href}
              target="_blank"
              rel="noopener noreferrer"
              {...props}
            >
              {children}
            </a>
          ),
          // 代码块增强：使用 CodeBlock 组件
          code: (props) => {
            const { node, inline, className, children, ...rest } = props as any;

            // 行内代码：直接返回原生 code 标签
            if (inline || !className) {
              return <code className={className}>{children}</code>;
            }

            // 代码块：使用增强的 CodeBlock 组件
            const match = /language-(\w+)/.exec(className || '');
            const language = match ? match[1] : 'text';

            // 直接传递 children，让 CodeBlock 处理
            // rehype-highlight 已经将代码转换为带高亮的 React 元素
            return (
              <CodeBlock
                code={children}
                language={language}
                inline={false}
                className={className}
                enableMermaid={enableMermaid}
              />
            );
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

export default MarkdownViewer;

