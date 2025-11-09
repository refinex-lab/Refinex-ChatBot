"use client";

import {useEffect, useRef, useState} from "react";
import mermaid from "mermaid";
import {Loader2} from "lucide-react";

// Initialize Mermaid configuration
mermaid.initialize({
  startOnLoad: false,
  theme: "default",
  securityLevel: "loose",
  fontFamily:
    '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial',
  flowchart: {
    useMaxWidth: true,
    htmlLabels: true,
    curve: "basis",
  },
  sequence: {
    useMaxWidth: true,
    wrap: true,
  },
  gantt: {
    useMaxWidth: true,
  },
});

export interface MermaidRendererProps {
  chart: string;
  className?: string;
}

const isChartLikelyComplete = (chart: string): boolean => {
  if (!chart || chart.trim().length < 10) {
    return false;
  }

  const trimmed = chart.trim();
  const lines = trimmed.split("\n").filter((line) => line.trim());

  if (lines.length < 2) {
    return false;
  }

  const lastLine = lines.at(-1)?.trim() || "";

  const openBrackets = (lastLine.match(/\[/g) || []).length;
  const closeBrackets = (lastLine.match(/\]/g) || []).length;
  if (openBrackets > closeBrackets) {
    return false;
  }

  if (lastLine.endsWith("-->") || lastLine.endsWith("->") || lastLine.endsWith("-")) {
    return false;
  }

  const quotes = (trimmed.match(/"/g) || []).length;
  if (quotes % 2 !== 0) {
    return false;
  }

  return true;
};

export const MermaidRenderer = ({ chart, className }: MermaidRendererProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const renderTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

  useEffect(() => {
    if (renderTimeoutRef.current) {
      clearTimeout(renderTimeoutRef.current);
    }

    const renderChart = async () => {
      if (!containerRef.current || !chart) {
        setLoading(false);
        return;
      }

      if (!isChartLikelyComplete(chart)) {
        setLoading(true);
        setError(null);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        const id = `mermaid-${Math.random().toString(36).slice(2, 11)}`;
        const { svg } = await mermaid.render(id, chart);

        if (containerRef.current) {
          containerRef.current.innerHTML = svg;
        }

        setLoading(false);
      } catch (err) {
        console.warn("Mermaid render failed (may be streaming):", err);

        if (chart.trim().length > 50 && isChartLikelyComplete(chart)) {
          setError("图表语法错误，请检查 Mermaid 语法");
        }

        setLoading(false);
      }
    };

    renderTimeoutRef.current = setTimeout(renderChart, 300);

    return () => {
      if (renderTimeoutRef.current) {
        clearTimeout(renderTimeoutRef.current);
      }
    };
  }, [chart]);

  if (error) {
    return (
      <div className={`rounded-lg bg-red-50 dark:bg-red-950/20 p-4 ${className || ""}`}>
        <div className="flex items-center gap-2 mb-3 text-red-700 dark:text-red-400">
          <span className="text-lg">⚠️</span>
          <span className="font-medium">{error}</span>
        </div>
        <pre className="text-xs text-muted-foreground bg-background p-3 rounded border overflow-x-auto">
          {chart}
        </pre>
      </div>
    );
  }

  return (
    <div className={`relative rounded-lg text-center overflow-x-auto ${className || ""}`}>
      {loading && (
        <div className="flex items-center justify-center min-h-[200px] text-muted-foreground">
          <Loader2 className="size-6 animate-spin mr-2" />
          <span>正在渲染图表...</span>
        </div>
      )}
      <div
        ref={containerRef}
        className="flex justify-center items-center"
        style={{ display: loading ? "none" : "flex" }}
      />
    </div>
  );
};

