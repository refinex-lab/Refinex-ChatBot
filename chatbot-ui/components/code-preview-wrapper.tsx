"use client";

import type {ReactNode} from "react";
import {CodePreviewProvider} from "@/contexts/code-preview-context";

export function CodePreviewWrapper({ children }: { children: ReactNode }) {
  return <CodePreviewProvider>{children}</CodePreviewProvider>;
}

