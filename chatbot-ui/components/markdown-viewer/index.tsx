"use client";

import type {ReactNode} from "react";
import {useMemo} from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import rehypeRaw from "rehype-raw";
import rehypeSanitize from "rehype-sanitize";
import {CodeBlock} from "./code-block";
import {cn} from "@/lib/utils";
import "highlight.js/styles/github.css";

export interface MarkdownViewerProps {
  content?: string;
  className?: string;
  style?: React.CSSProperties;
  bordered?: boolean;
  enableHighlight?: boolean;
  allowHtml?: boolean;
  enableMermaid?: boolean;
  emptyText?: string;
}

export const MarkdownViewer = ({
  content,
  className,
  style,
  bordered = false,
  enableHighlight = true,
  allowHtml = false,
  enableMermaid = true,
  emptyText = "暂无内容",
}: MarkdownViewerProps) => {
  const rehypePlugins = useMemo(() => {
    const plugins: Array<
      | typeof rehypeRaw
      | typeof rehypeSanitize
      | typeof rehypeHighlight
    > = [];

    if (allowHtml) {
      plugins.push(rehypeRaw);
      plugins.push(rehypeSanitize);
    }

    if (enableHighlight) {
      plugins.push(rehypeHighlight);
    }

    return plugins;
  }, [allowHtml, enableHighlight]);

  if (!content || content.trim() === "") {
    return (
      <div
        className={cn("flex items-center justify-center min-h-[200px]", className)}
        style={style}
      >
        <div className="text-center">
          <p className="text-muted-foreground">{emptyText}</p>
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        "markdown-viewer w-full prose prose-sm max-w-none dark:prose-invert",
        {
          "p-6 border rounded-lg bg-background": bordered,
        },
        className
      )}
      style={style}
    >
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={rehypePlugins}
        components={{
          table: ({ children }) => (
            <div className="overflow-x-auto my-4">
              <table className="min-w-full border-collapse">{children}</table>
            </div>
          ),
          a: ({ href, children, ...props }) => (
            <a
              href={href}
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary hover:underline"
              {...props}
            >
              {children}
            </a>
          ),
          code: (props) => {
            const { node, inline, className, children, ...rest } = props as {
              node?: unknown;
              inline?: boolean;
              className?: string;
              children?: ReactNode;
            };

            if (inline || !className) {
              return (
                <code className="px-1.5 py-0.5 mx-0.5 text-xs !text-foreground font-mono rounded">
                  {children}
                </code>
              );
            }

            const match = /language-(\w+)/.exec(className || "");
            const language = match ? match[1] : "text";

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
          h1: ({ children }) => (
            <h1 className="text-3xl font-semibold mt-6 mb-4 first:mt-0">{children}</h1>
          ),
          h2: ({ children }) => (
            <h2 className="text-2xl font-semibold mt-5 mb-3 pb-2 border-b">
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3 className="text-xl font-semibold mt-4 mb-2">{children}</h3>
          ),
          h4: ({ children }) => (
            <h4 className="text-lg font-semibold mt-3 mb-2">{children}</h4>
          ),
          h5: ({ children }) => (
            <h5 className="text-base font-semibold mt-3 mb-2">{children}</h5>
          ),
          h6: ({ children }) => (
            <h6 className="text-sm font-semibold mt-3 mb-2 text-muted-foreground">
              {children}
            </h6>
          ),
          p: ({ children }) => <p className="my-3 leading-7">{children}</p>,
          ul: ({ children }) => (
            <ul className="my-3 ml-6 list-disc space-y-1">{children}</ul>
          ),
          ol: ({ children }) => (
            <ol className="my-3 ml-6 list-decimal space-y-1">{children}</ol>
          ),
          li: ({ children }) => <li className="leading-7">{children}</li>,
          blockquote: ({ children }) => (
            <blockquote className="my-4 pl-4 border-l-4 border-muted-foreground/30 text-muted-foreground italic bg-muted/30 py-2 rounded-r">
              {children}
            </blockquote>
          ),
          hr: () => <hr className="my-6 border-t border-border" />,
          img: ({ src, alt }) => (
            <img
              src={src}
              alt={alt}
              className="max-w-full h-auto rounded-md shadow-sm my-4"
            />
          ),
          pre: ({ children }) => (
            <pre className="m-0 p-0 !bg-white dark:!bg-zinc-950">{children}</pre>
          ),
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};

