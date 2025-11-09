"use client";

import {useCallback, useEffect, useRef, useState} from "react";
import * as Dialog from "@radix-ui/react-dialog";
import * as DropdownMenu from "@radix-ui/react-dropdown-menu";
import * as VisuallyHidden from "@radix-ui/react-visually-hidden";
import {CrossIcon, DownloadIcon, FullscreenIcon,} from "@/components/icons";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip";
import {MermaidRenderer} from "./mermaid-renderer";
import {toast} from "sonner";
import {cn} from "@/lib/utils";

export interface MermaidPreviewModalProps {
  visible: boolean;
  onClose: () => void;
  chart: string;
  language?: string;
}

export const MermaidPreviewModal = ({
  visible,
  onClose,
  chart,
  language = "mermaid",
}: MermaidPreviewModalProps) => {
  const [scale, setScale] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });

  const containerRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);

  const ZOOM_STEP = 0.1;
  const MIN_SCALE = 0.1;
  const MAX_SCALE = 5;

  const resetView = useCallback(() => {
    setScale(1);
    setRotation(0);
    setPosition({ x: 0, y: 0 });
  }, []);

  const zoomIn = useCallback(() => {
    setScale((prev) => Math.min(prev + ZOOM_STEP, MAX_SCALE));
  }, []);

  const zoomOut = useCallback(() => {
    setScale((prev) => Math.max(prev - ZOOM_STEP, MIN_SCALE));
  }, []);

  const rotate = useCallback(() => {
    setRotation((prev) => (prev + 90) % 360);
  }, []);

  const fitToScreen = useCallback(() => {
    if (!containerRef.current || !contentRef.current) return;

    const container = containerRef.current;
    const content = contentRef.current;

    const containerWidth = container.clientWidth;
    const containerHeight = container.clientHeight;

    const svgElement = content.querySelector("svg");
    if (!svgElement) return;

    const bbox = svgElement.getBBox();
    const contentWidth = bbox.width + 48;
    const contentHeight = bbox.height + 48;

    const scaleX = (containerWidth * 0.8) / contentWidth;
    const scaleY = (containerHeight * 0.8) / contentHeight;
    const newScale = Math.min(scaleX, scaleY, 1);

    setScale(newScale);
    setPosition({ x: 0, y: 0 });
  }, []);

  const downloadAsSource = useCallback(() => {
    try {
      const blob = new Blob([chart], { type: "text/plain;charset=utf-8" });
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
  }, [chart]);

  const downloadAsSVG = useCallback(() => {
    try {
      const svgElement = contentRef.current?.querySelector("svg");
      if (!svgElement) {
        toast.error("未找到 SVG 元素");
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
  }, []);

  const downloadAsPNG = useCallback(() => {
    try {
      const svgElement = contentRef.current?.querySelector("svg");
      if (!svgElement) {
        toast.error("未找到 SVG 元素");
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
  }, []);

  const handleWheel = useCallback((e: WheelEvent) => {
    e.preventDefault();
    const delta = e.deltaY > 0 ? -ZOOM_STEP : ZOOM_STEP;
    setScale((prev) => Math.max(MIN_SCALE, Math.min(MAX_SCALE, prev + delta)));
  }, []);

  const handleMouseDown = useCallback(
    (e: React.MouseEvent) => {
      if (e.button === 0) {
        setIsDragging(true);
        setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y });
      }
    },
    [position]
  );

  const handleMouseMove = useCallback(
    (e: MouseEvent) => {
      if (isDragging) {
        setPosition({
          x: e.clientX - dragStart.x,
          y: e.clientY - dragStart.y,
        });
      }
    },
    [isDragging, dragStart]
  );

  const handleMouseUp = useCallback(() => {
    setIsDragging(false);
  }, []);

  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      } else if (e.key === "+" || e.key === "=") {
        e.preventDefault();
        zoomIn();
      } else if (e.key === "-" || e.key === "_") {
        e.preventDefault();
        zoomOut();
      } else if (e.key === "r" || e.key === "R") {
        e.preventDefault();
        rotate();
      } else if (e.key === "0") {
        e.preventDefault();
        resetView();
      } else if (e.key === "f" || e.key === "F") {
        e.preventDefault();
        fitToScreen();
      }
    },
    [onClose, zoomIn, zoomOut, rotate, resetView, fitToScreen]
  );

  useEffect(() => {
    if (!visible) return;

    const container = containerRef.current;
    if (container) {
      container.addEventListener("wheel", handleWheel, { passive: false });
    }

    document.addEventListener("mousemove", handleMouseMove);
    document.addEventListener("mouseup", handleMouseUp);
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      if (container) {
        container.removeEventListener("wheel", handleWheel);
      }
      document.removeEventListener("mousemove", handleMouseMove);
      document.removeEventListener("mouseup", handleMouseUp);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [visible, handleWheel, handleMouseMove, handleMouseUp, handleKeyDown]);

  useEffect(() => {
    if (visible) {
      resetView();
      setTimeout(() => {
        fitToScreen();
      }, 200);
    }
  }, [visible, resetView, fitToScreen]);

  return (
    <Dialog.Root open={visible} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/80 z-50" />
        <Dialog.Content className="fixed inset-0 z-50 flex flex-col bg-background">
          <VisuallyHidden.Root>
            <Dialog.Title>Mermaid 图表预览</Dialog.Title>
          </VisuallyHidden.Root>
          <TooltipProvider>
            {/* Toolbar */}
            <div className="flex items-center justify-between px-4 py-3 border-b bg-muted/50">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium min-w-[50px] text-center">
                  {Math.round(scale * 100)}%
                </span>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={zoomIn}
                      disabled={scale >= MAX_SCALE}
                      className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent disabled:opacity-50 transition-colors"
                    >
                      <span className="text-base font-semibold leading-none">+</span>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>放大 (+)</TooltipContent>
                </Tooltip>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={zoomOut}
                      disabled={scale <= MIN_SCALE}
                      className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent disabled:opacity-50 transition-colors"
                    >
                      <span className="text-base font-semibold leading-none">−</span>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>缩小 (-)</TooltipContent>
                </Tooltip>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={fitToScreen}
                      className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    >
                      <FullscreenIcon size={16} />
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>适应屏幕 (F)</TooltipContent>
                </Tooltip>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={resetView}
                      className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    >
                      <span className="text-sm font-medium">100%</span>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>实际大小 (0)</TooltipContent>
                </Tooltip>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={rotate}
                      className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    >
                      <svg
                        className="size-4"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        viewBox="0 0 16 16"
                      >
                        <path
                          d="M8 2v4M8 10v4M2 8h4M10 8h4M4.343 4.343l2.828 2.828M8.829 8.829l2.828 2.828M11.657 4.343l-2.828 2.828M7.171 8.829l-2.828 2.828"
                          strokeLinecap="round"
                        />
                      </svg>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>旋转 90° (R)</TooltipContent>
                </Tooltip>

                <DropdownMenu.Root>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <DropdownMenu.Trigger asChild>
                        <button
                          type="button"
                          className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                        >
                          <DownloadIcon size={16} />
                        </button>
                      </DropdownMenu.Trigger>
                    </TooltipTrigger>
                    <TooltipContent>下载</TooltipContent>
                  </Tooltip>

                  <DropdownMenu.Portal>
                    <DropdownMenu.Content className="min-w-[180px] bg-popover border rounded-md shadow-md p-1 z-50">
                      <DropdownMenu.Item
                        onClick={downloadAsSource}
                        className="px-3 py-2 text-sm cursor-pointer rounded hover:bg-accent outline-none"
                      >
                        下载源码 (.mmd)
                      </DropdownMenu.Item>
                      <DropdownMenu.Item
                        onClick={downloadAsSVG}
                        className="px-3 py-2 text-sm cursor-pointer rounded hover:bg-accent outline-none"
                      >
                        下载 SVG 图片
                      </DropdownMenu.Item>
                      <DropdownMenu.Item
                        onClick={downloadAsPNG}
                        className="px-3 py-2 text-sm cursor-pointer rounded hover:bg-accent outline-none"
                      >
                        下载 PNG 图片
                      </DropdownMenu.Item>
                    </DropdownMenu.Content>
                  </DropdownMenu.Portal>
                </DropdownMenu.Root>
              </div>

              <div className="flex items-center gap-3">
                <span className="text-xs text-muted-foreground hidden md:block">
                  快捷键：ESC 关闭 | +/- 缩放 | R 旋转 | F 适应屏幕 | 0 实际大小 | 鼠标滚轮缩放 | 拖拽移动
                </span>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={onClose}
                      className="p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    >
                      <CrossIcon size={16} />
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>关闭 (ESC)</TooltipContent>
                </Tooltip>
              </div>
            </div>

            {/* Preview Container */}
            <div
              ref={containerRef}
              onMouseDown={handleMouseDown}
              className={cn(
                "flex-1 relative overflow-hidden flex items-center justify-center bg-muted/30",
                isDragging ? "cursor-grabbing" : "cursor-grab"
              )}
              style={{
                backgroundImage: `
                  linear-gradient(45deg, hsl(var(--muted)) 25%, transparent 25%),
                  linear-gradient(-45deg, hsl(var(--muted)) 25%, transparent 25%),
                  linear-gradient(45deg, transparent 75%, hsl(var(--muted)) 75%),
                  linear-gradient(-45deg, transparent 75%, hsl(var(--muted)) 75%)
                `,
                backgroundSize: "20px 20px",
                backgroundPosition: "0 0, 0 10px, 10px -10px, -10px 0px",
              }}
            >
              <div
                ref={contentRef}
                style={{
                  transform: `translate(${position.x}px, ${position.y}px) scale(${scale}) rotate(${rotation}deg)`,
                  transformOrigin: "center center",
                  transition: isDragging ? "none" : "transform 0.2s ease-out",
                }}
              >
                <div className="inline-block bg-background p-6 rounded-lg shadow-xl">
                  <MermaidRenderer chart={chart} />
                </div>
              </div>
            </div>
          </TooltipProvider>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
};

