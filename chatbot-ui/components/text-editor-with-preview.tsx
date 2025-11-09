"use client";

import {useState} from "react";
import {Editor} from "./text-editor";
import {MarkdownViewer} from "./markdown-viewer";
import * as Tooltip from "@radix-ui/react-tooltip";
import {Eye, Pencil} from "lucide-react";
import type {Suggestion} from "@/lib/db/schema";
import {cn} from "@/lib/utils";

type TextEditorWithPreviewProps = {
  content: string;
  onSaveContent: (updatedContent: string, debounce: boolean) => void;
  status: "streaming" | "idle";
  isCurrentVersion: boolean;
  currentVersionIndex: number;
  suggestions: Suggestion[];
};

export const TextEditorWithPreview = ({
  content,
  onSaveContent,
  status,
  isCurrentVersion,
  currentVersionIndex,
  suggestions,
}: TextEditorWithPreviewProps) => {
  const [mode, setMode] = useState<"edit" | "preview">("edit");

  return (
    <div className="flex flex-col h-full">
      {/* Mode Toggle */}
      <Tooltip.Provider>
        <div className="flex items-center gap-2 pb-4 border-b mb-4">
          <Tooltip.Root>
            <Tooltip.Trigger asChild>
              <button
                type="button"
                onClick={() => setMode("edit")}
                className={cn(
                  "flex items-center gap-2 px-3 py-1.5 rounded-md transition-colors text-sm",
                  mode === "edit"
                    ? "bg-primary text-primary-foreground"
                    : "hover:bg-accent"
                )}
              >
                <Pencil className="size-4" />
                <span>编辑</span>
              </button>
            </Tooltip.Trigger>
            <Tooltip.Content>编辑模式</Tooltip.Content>
          </Tooltip.Root>

          <Tooltip.Root>
            <Tooltip.Trigger asChild>
              <button
                type="button"
                onClick={() => setMode("preview")}
                className={cn(
                  "flex items-center gap-2 px-3 py-1.5 rounded-md transition-colors text-sm",
                  mode === "preview"
                    ? "bg-primary text-primary-foreground"
                    : "hover:bg-accent"
                )}
              >
                <Eye className="size-4" />
                <span>预览</span>
              </button>
            </Tooltip.Trigger>
            <Tooltip.Content>预览模式</Tooltip.Content>
          </Tooltip.Root>
        </div>
      </Tooltip.Provider>

      {/* Content */}
      <div className="flex-1 overflow-auto">
        {mode === "edit" ? (
          <Editor
            content={content}
            currentVersionIndex={currentVersionIndex}
            isCurrentVersion={isCurrentVersion}
            onSaveContent={onSaveContent}
            status={status}
            suggestions={suggestions}
          />
        ) : (
          <MarkdownViewer
            content={content}
            enableHighlight={true}
            enableMermaid={true}
            allowHtml={false}
          />
        )}
      </div>
    </div>
  );
};

