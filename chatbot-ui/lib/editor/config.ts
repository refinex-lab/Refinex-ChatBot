import {textblockTypeInputRule} from "prosemirror-inputrules";
import {Schema} from "prosemirror-model";
import {schema} from "prosemirror-schema-basic";
import {addListNodes} from "prosemirror-schema-list";
import type {Transaction} from "prosemirror-state";
import type {EditorView} from "prosemirror-view";
import type {MutableRefObject} from "react";

import {buildContentFromDocument} from "./functions";

// 创建文档 schema，包含列表和代码块支持
const schemaWithLists = addListNodes(schema.spec.nodes, "paragraph block*", "block");

// 扩展 code_block 节点以更好地处理从 HTML 解析
const nodesWithCodeBlock = schemaWithLists.update("code_block", {
  ...schemaWithLists.get("code_block"),
  parseDOM: [
    {
      tag: "pre",
      preserveWhitespace: "full",
      getAttrs: (node: HTMLElement | string) => {
        if (typeof node === "string") return null;
        
        // 尝试从 <pre> 标签或其子 <code> 标签中获取语言信息
        const codeElement = node.querySelector("code");
        const className = codeElement?.className || node.className || "";
        const languageMatch = /language-(\w+)/.exec(className);
        
        return {
          params: languageMatch?.[1] || null,
        };
      },
    },
    {
      tag: "div.highlight pre",
      preserveWhitespace: "full",
    },
  ],
});

export const documentSchema = new Schema({
  nodes: nodesWithCodeBlock,
  marks: schema.spec.marks,
});

export function headingRule(level: number) {
  return textblockTypeInputRule(
    new RegExp(`^(#{1,${level}})\\s$`),
    documentSchema.nodes.heading,
    () => ({ level })
  );
}

export const handleTransaction = ({
  transaction,
  editorRef,
  onSaveContent,
}: {
  transaction: Transaction;
  editorRef: MutableRefObject<EditorView | null>;
  onSaveContent: (updatedContent: string, debounce: boolean) => void;
}) => {
  if (!editorRef || !editorRef.current) {
    return;
  }

  const newState = editorRef.current.state.apply(transaction);
  editorRef.current.updateState(newState);

  if (transaction.docChanged && !transaction.getMeta("no-save")) {
    const updatedContent = buildContentFromDocument(newState.doc);

    if (transaction.getMeta("no-debounce")) {
      onSaveContent(updatedContent, false);
    } else {
      onSaveContent(updatedContent, true);
    }
  }
};
