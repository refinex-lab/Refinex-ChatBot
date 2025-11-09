"use client";

import {defaultMarkdownSerializer, MarkdownSerializer} from "prosemirror-markdown";
import {DOMParser, type Node} from "prosemirror-model";
import {Decoration, DecorationSet, type EditorView} from "prosemirror-view";
import {renderToString} from "react-dom/server";

import {Response} from "@/components/elements/response";

import {documentSchema} from "./config";
import {createSuggestionWidget, type UISuggestion} from "./suggestions";

// 创建自定义的 Markdown 序列化器，确保正确处理代码块
const customMarkdownSerializer = new MarkdownSerializer(
  {
    ...defaultMarkdownSerializer.nodes,
    // 确保代码块被正确序列化
    code_block(state, node) {
      state.write(`\`\`\`${node.attrs.params || ""}\n`);
      state.text(node.textContent, false);
      state.ensureNewLine();
      state.write("```");
      state.closeBlock(node);
    },
  },
  defaultMarkdownSerializer.marks
);

export const buildDocumentFromContent = (content: string) => {
  const parser = DOMParser.fromSchema(documentSchema);
  const stringFromMarkdown = renderToString(<Response>{content}</Response>);
  const tempContainer = document.createElement("div");
  tempContainer.innerHTML = stringFromMarkdown;
  
  return parser.parse(tempContainer);
};

export const buildContentFromDocument = (document: Node) => {
  return customMarkdownSerializer.serialize(document);
};

export const createDecorations = (
  suggestions: UISuggestion[],
  view: EditorView
) => {
  const decorations: Decoration[] = [];

  for (const suggestion of suggestions) {
    decorations.push(
      Decoration.inline(
        suggestion.selectionStart,
        suggestion.selectionEnd,
        {
          class: "suggestion-highlight",
        },
        {
          suggestionId: suggestion.id,
          type: "highlight",
        }
      )
    );

    decorations.push(
      Decoration.widget(
        suggestion.selectionStart,
        (currentView) => {
          const { dom } = createSuggestionWidget(suggestion, currentView);
          return dom;
        },
        {
          suggestionId: suggestion.id,
          type: "widget",
        }
      )
    );
  }

  return DecorationSet.create(view.state.doc, decorations);
};
