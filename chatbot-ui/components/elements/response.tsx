"use client";

import type {ReactNode} from "react";
import {memo} from "react";
import {MarkdownViewer} from "@/components/markdown-viewer";
import {cn} from "@/lib/utils";

type ResponseProps = {
  className?: string;
  children?: string | ReactNode;
  enableMermaid?: boolean;
};

export const Response = memo(
  ({ className, children, enableMermaid = true }: ResponseProps) => {
    // Convert children to string
    const content = typeof children === "string" ? children : String(children ?? "");

    return (
      <div
        className={cn(
          "size-full [&>*:first-child]:mt-0 [&>*:last-child]:mb-0",
          className
        )}
      >
        <MarkdownViewer
          content={content}
          enableMermaid={enableMermaid}
          className="[&_code]:whitespace-pre-wrap [&_code]:break-words [&_pre]:max-w-full [&_pre]:overflow-x-auto"
        />
      </div>
    );
  },
  (prevProps, nextProps) => {
    const prevContent = typeof prevProps.children === "string" 
      ? prevProps.children 
      : String(prevProps.children ?? "");
    const nextContent = typeof nextProps.children === "string" 
      ? nextProps.children 
      : String(nextProps.children ?? "");
    return prevContent === nextContent && prevProps.enableMermaid === nextProps.enableMermaid;
  }
);

Response.displayName = "Response";
