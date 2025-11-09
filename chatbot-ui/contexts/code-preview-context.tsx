"use client";

import {createContext, useCallback, useContext, useState} from "react";
import type {CodeType} from "@/components/markdown-viewer/types";

interface CodePreviewState {
  visible: boolean;
  code: string;
  language: string;
  codeType: CodeType | null;
}

interface CodePreviewContextType {
  state: CodePreviewState;
  openPreview: (code: string, language: string, codeType: CodeType) => void;
  closePreview: () => void;
}

const CodePreviewContext = createContext<CodePreviewContextType | undefined>(undefined);

export function CodePreviewProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<CodePreviewState>({
    visible: false,
    code: "",
    language: "",
    codeType: null,
  });


  const openPreview = useCallback((code: string, language: string, codeType: CodeType) => {
    setState({
      visible: true,
      code,
      language,
      codeType,
    });
  }, []);

  const closePreview = useCallback(() => {
    setState((prev) => ({
      ...prev,
      visible: false,
    }));
  }, []);

  return (
    <CodePreviewContext.Provider value={{ state, openPreview, closePreview }}>
      {children}
    </CodePreviewContext.Provider>
  );
}

export function useCodePreview() {
  const context = useContext(CodePreviewContext);
  if (context === undefined) {
    // 添加调试信息
    if (typeof window !== "undefined") {
      console.error("[useCodePreview] Context is undefined. Component tree:", {
        hasProvider: context !== undefined,
        windowLocation: window.location.pathname,
      });
    }
    throw new Error("useCodePreview must be used within a CodePreviewProvider");
  }
  return context;
}

// 安全的 hook 包装器，用于在 provider 可能不存在的情况下
export function useCodePreviewSafe() {
  const context = useContext(CodePreviewContext);
  if (context === undefined) {
    if (typeof window !== "undefined") {
      //console.warn("[useCodePreviewSafe] Context is undefined, using fallback");
    }
    // 返回一个默认的 no-op 实现
    return {
      state: {
        visible: false,
        code: "",
        language: "",
        codeType: null,
      },
      openPreview: () => {
        //console.warn("[useCodePreviewSafe] openPreview called but context is not available");
      },
      closePreview: () => {
        //console.warn("[useCodePreviewSafe] closePreview called but context is not available");
      },
    };
  }
  return context;
}

