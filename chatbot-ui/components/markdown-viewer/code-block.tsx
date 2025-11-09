"use client";

import type {ReactNode} from "react";
import {useRef, useState} from "react";
import * as DropdownMenu from "@radix-ui/react-dropdown-menu";
import {CheckCircleFillIcon, CodeIcon, CopyIcon, DownloadIcon, FullscreenIcon,} from "@/components/icons";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {MermaidRenderer} from "./mermaid-renderer";
import {MermaidPreviewModal} from "./mermaid-preview-modal";
import {toast} from "sonner";
import {cn} from "@/lib/utils";

export interface CodeBlockProps {
  code: string | ReactNode;
  language?: string;
  inline?: boolean;
  className?: string;
  enableMermaid?: boolean;
}

export const CodeBlock = ({
  code,
  language = "text",
  inline = false,
  className = "",
  enableMermaid = true,
}: CodeBlockProps) => {
  const [copied, setCopied] = useState(false);
  const [showSource, setShowSource] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const mermaidContainerRef = useRef<HTMLDivElement>(null);

  if (inline) {
    return <code className={className}>{code}</code>;
  }

  const isMermaid = enableMermaid && language?.toLowerCase() === "mermaid";

  const getPlainTextCode = (): string => {
    if (typeof code === "string") {
      return code;
    }
    if (Array.isArray(code)) {
      return extractTextFromReactElement(code);
    }
    return extractTextFromReactElement(code);
  };

  const extractTextFromReactElement = (element: unknown): string => {
    if (typeof element === "string") {
      return element;
    }
    if (typeof element === "number") {
      return String(element);
    }
    if (Array.isArray(element)) {
      return element.map(extractTextFromReactElement).join("");
    }
    if (element && typeof element === "object" && "props" in element) {
      const props = (element as { props?: { children?: unknown } }).props;
      if (props?.children) {
        return extractTextFromReactElement(props.children);
      }
      return "";
    }
    if (element === null || element === undefined || typeof element === "boolean") {
      return "";
    }
    if (typeof element === "object") {
      return "";
    }
    return String(element);
  };

  const handleCopy = async () => {
    try {
      const plainText = getPlainTextCode();
      await navigator.clipboard.writeText(plainText);
      setCopied(true);
      toast.success("复制成功");
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      toast.error("复制失败");
      console.error("复制失败:", err);
    }
  };

  const handleDownload = () => {
    try {
      const plainText = getPlainTextCode();

      const extensionMap: Record<string, string> = {
        javascript: "js",
        typescript: "ts",
        python: "py",
        java: "java",
        cpp: "cpp",
        csharp: "cs",
        go: "go",
        rust: "rs",
        php: "php",
        ruby: "rb",
        swift: "swift",
        kotlin: "kt",
        scala: "scala",
        html: "html",
        css: "css",
        scss: "scss",
        less: "less",
        json: "json",
        xml: "xml",
        yaml: "yaml",
        markdown: "md",
        sql: "sql",
        shell: "sh",
        bash: "sh",
        powershell: "ps1",
        mermaid: "mmd",
      };

      const ext = extensionMap[language.toLowerCase()] || "txt";
      const filename = `code.${ext}`;

      const blob = new Blob([plainText], { type: "text/plain;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      toast.success("下载成功");
    } catch (err) {
      toast.error("下载失败");
      console.error("下载失败:", err);
    }
  };

  const downloadMermaidAsSource = () => {
    try {
      const plainText = getPlainTextCode();
      const blob = new Blob([plainText], { type: "text/plain;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "mermaid-diagram.mmd";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      toast.success("源码下载成功");
    } catch (err) {
      toast.error("源码下载失败");
      console.error("下载失败:", err);
    }
  };

  const downloadMermaidAsSVG = () => {
    try {
      const svgElement = mermaidContainerRef.current?.querySelector("svg");
      if (!svgElement) {
        toast.error("未找到 SVG 元素，请等待图表渲染完成");
        return;
      }

      const clonedSvg = svgElement.cloneNode(true) as SVGElement;
      clonedSvg.setAttribute("xmlns", "http://www.w3.org/2000/svg");

      const svgData = new XMLSerializer().serializeToString(clonedSvg);
      const blob = new Blob([svgData], { type: "image/svg+xml;charset=utf-8" });
      const url = URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = "mermaid-diagram.svg";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      toast.success("SVG 下载成功");
    } catch (err) {
      toast.error("SVG 下载失败");
      console.error("下载失败:", err);
    }
  };

  const downloadMermaidAsPNG = () => {
    try {
      const svgElement = mermaidContainerRef.current?.querySelector("svg");
      if (!svgElement) {
        toast.error("未找到 SVG 元素，请等待图表渲染完成");
        return;
      }

      const bbox = svgElement.getBBox();
      const width = bbox.width;
      const height = bbox.height;

      const canvas = document.createElement("canvas");
      const scale = 2;
      canvas.width = width * scale;
      canvas.height = height * scale;
      const ctx = canvas.getContext("2d", { willReadFrequently: false });

      if (!ctx) {
        toast.error("无法创建 Canvas 上下文");
        return;
      }

      ctx.fillStyle = "#ffffff";
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.scale(scale, scale);

      const clonedSvg = svgElement.cloneNode(true) as SVGElement;
      clonedSvg.setAttribute("xmlns", "http://www.w3.org/2000/svg");

      const svgData = new XMLSerializer().serializeToString(clonedSvg);
      const svgBlob = new Blob([svgData], { type: "image/svg+xml;charset=utf-8" });
      const url = URL.createObjectURL(svgBlob);

      const img = new Image();
      img.addEventListener("load", () => {
        ctx.drawImage(img, 0, 0);
        URL.revokeObjectURL(url);

        canvas.toBlob((blob) => {
          if (blob) {
            const pngUrl = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = pngUrl;
            link.download = "mermaid-diagram.png";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(pngUrl);
            toast.success("PNG 下载成功");
          } else {
            toast.error("PNG 生成失败");
          }
        }, "image/png");
      });

      img.addEventListener("error", () => {
        URL.revokeObjectURL(url);
        toast.error("图片加载失败");
      });

      img.src = url;
    } catch (err) {
      toast.error("PNG 下载失败");
      console.error("下载失败:", err);
    }
  };

  const handleToggleSource = () => {
    setShowSource(!showSource);
  };

  const handleOpenPreview = () => {
    setPreviewVisible(true);
  };

  const handleClosePreview = () => {
    setPreviewVisible(false);
  };

  const getLanguageDisplayName = (lang: string): string => {
    const nameMap: Record<string, string> = {
      javascript: "JavaScript",
      typescript: "TypeScript",
      python: "Python",
      java: "Java",
      cpp: "C++",
      csharp: "C#",
      go: "Go",
      rust: "Rust",
      php: "PHP",
      ruby: "Ruby",
      swift: "Swift",
      kotlin: "Kotlin",
      scala: "Scala",
      html: "HTML",
      css: "CSS",
      scss: "SCSS",
      less: "Less",
      json: "JSON",
      xml: "XML",
      yaml: "YAML",
      markdown: "Markdown",
      sql: "SQL",
      shell: "Shell",
      bash: "Bash",
      powershell: "PowerShell",
      mermaid: "Mermaid",
      text: "Text",
    };

    return nameMap[lang.toLowerCase()] || lang.toUpperCase();
  };

  return (
    <>
      <TooltipProvider>
        <div
          className={cn(
            "code-block-enhanced relative !rounded-xl overflow-hidden !border !border-gray-200 dark:!border-zinc-800",
            isMermaid ? "border-0" : "bg-gray-50 dark:bg-zinc-900/30"
          )}
        >
          {/* Toolbar */}
          <div
            className={cn(
              "flex items-center justify-between px-3 py-2",
              isMermaid
                ? "bg-transparent"
                : "bg-gray-100/60 dark:bg-zinc-800/40"
            )}
          >
            <div className="text-xs font-medium text-muted-foreground uppercase tracking-wide font-mono">
              {getLanguageDisplayName(language)}
            </div>

            <div className="flex items-center gap-1">
              {isMermaid && (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={handleOpenPreview}
                      className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                      aria-label="大屏预览"
                    >
                      <FullscreenIcon size={14} />
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>大屏预览</TooltipContent>
                </Tooltip>
              )}

              {isMermaid && (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={handleToggleSource}
                      className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                      aria-label={showSource ? "查看图表" : "查看源码"}
                    >
                      <CodeIcon size={14} />
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>
                    {showSource ? "查看图表" : "查看源码"}
                  </TooltipContent>
                </Tooltip>
              )}

              <Tooltip>
                <TooltipTrigger asChild>
                  <button
                    type="button"
                    onClick={handleCopy}
                    className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    aria-label={copied ? "已复制" : "复制代码"}
                  >
                    {copied ? (
                      <span className="text-green-600 dark:text-green-400">
                        <CheckCircleFillIcon size={14} />
                      </span>
                    ) : (
                      <CopyIcon size={14} />
                    )}
                  </button>
                </TooltipTrigger>
                <TooltipContent>{copied ? "已复制" : "复制代码"}</TooltipContent>
              </Tooltip>

              {isMermaid ? (
                <DropdownMenu.Root>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <DropdownMenu.Trigger asChild>
                        <button
                          type="button"
                          className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                          aria-label="下载"
                        >
                          <DownloadIcon size={14} />
                        </button>
                      </DropdownMenu.Trigger>
                    </TooltipTrigger>
                    <TooltipContent>下载</TooltipContent>
                  </Tooltip>

                  <DropdownMenu.Portal>
                    <DropdownMenu.Content className="min-w-[180px] bg-popover border rounded-md shadow-md p-1 z-50">
                      <DropdownMenu.Item
                        onClick={downloadMermaidAsSource}
                        className="px-3 py-2 text-sm cursor-pointer rounded hover:bg-accent outline-none"
                      >
                        下载源码 (.mmd)
                      </DropdownMenu.Item>
                      <DropdownMenu.Item
                        onClick={downloadMermaidAsSVG}
                        className="px-3 py-2 text-sm cursor-pointer rounded hover:bg-accent outline-none"
                      >
                        下载 SVG 图片
                      </DropdownMenu.Item>
                      <DropdownMenu.Item
                        onClick={downloadMermaidAsPNG}
                        className="px-3 py-2 text-sm cursor-pointer rounded hover:bg-accent outline-none"
                      >
                        下载 PNG 图片
                      </DropdownMenu.Item>
                    </DropdownMenu.Content>
                  </DropdownMenu.Portal>
                </DropdownMenu.Root>
              ) : (
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={handleDownload}
                      className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                      aria-label="下载代码"
                    >
                      <DownloadIcon size={14} />
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>下载代码</TooltipContent>
                </Tooltip>
              )}
            </div>
          </div>

          {/* Content */}
          {isMermaid && !showSource ? (
            <div ref={mermaidContainerRef} className="p-0 !bg-white dark:!bg-zinc-950">
              <MermaidRenderer
                chart={getPlainTextCode()}
                className="code-block-mermaid"
              />
            </div>
          ) : (
            <pre className="!m-0 !p-0 overflow-x-auto text-sm leading-relaxed !bg-white dark:!bg-zinc-950 block">
              <code className={cn("hljs block p-4 !bg-transparent", `language-${language}`)}>{code}</code>
            </pre>
          )}
        </div>
      </TooltipProvider>

      {isMermaid && (
        <MermaidPreviewModal
          visible={previewVisible}
          onClose={handleClosePreview}
          chart={getPlainTextCode()}
          language={language}
        />
      )}
    </>
  );
};

