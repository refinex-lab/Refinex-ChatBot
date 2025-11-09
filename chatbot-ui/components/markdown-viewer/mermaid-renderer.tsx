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

        // 清理和修复常见的 Mermaid 语法问题
        let cleanedChart = chart.trim();
        
        // 修复节点标签中的特殊字符问题
        // 匹配模式：节点ID[标签内容]，如果标签包含特殊字符但没有引号，添加引号
        cleanedChart = cleanedChart.replace(
          /(\w+)\[([^\]]+)\]/g,
          (match, nodeId, label) => {
            // 检查标签是否包含特殊字符（@, #, $, %, &, *, +, =, 等）
            const hasSpecialChars = /[@#\$%^&*+=\[\]{}|\\:;"'<>?,.\s]/.test(label);
            
            // 如果标签包含特殊字符且没有引号，添加引号
            if (hasSpecialChars && !label.startsWith('"') && !label.startsWith("'")) {
              // 转义标签中的引号
              const escapedLabel = label.replace(/"/g, '\\"').replace(/'/g, "\\'");
              return `${nodeId}["${escapedLabel}"]`;
            }
            return match;
          }
        );

        // 修复箭头语法：确保箭头前后有空格（如果缺少）
        // 匹配：节点ID箭头节点ID（没有空格的情况）
        cleanedChart = cleanedChart.replace(/(\w+)(-->|->|--|->>|==>|==>>)(\w+)/g, '$1 $2 $3');
        
        // 修复可能的未闭合标签：如果行以箭头结尾但没有目标节点
        cleanedChart = cleanedChart.split('\n').map(line => {
          const trimmed = line.trim();
          // 如果行以箭头结尾，可能是未完成的，跳过这行
          if (trimmed.endsWith('-->') || trimmed.endsWith('->') || trimmed.endsWith('--')) {
            return '';
          }
          return line;
        }).filter(line => line.trim()).join('\n');

        const id = `mermaid-${Math.random().toString(36).slice(2, 11)}`;
        const { svg } = await mermaid.render(id, cleanedChart);

        if (containerRef.current) {
          containerRef.current.innerHTML = svg;
        }

        setLoading(false);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : String(err);
        console.warn("Mermaid render failed (may be streaming):", err);

        // 提供更详细的错误信息
        if (chart.trim().length > 50 && isChartLikelyComplete(chart)) {
          let errorText = "图表语法错误，请检查 Mermaid 语法";
          
          // 提取错误行号（如果错误信息中包含）
          const lineMatch = errorMessage.match(/line (\d+)/i);
          if (lineMatch) {
            const lineNum = parseInt(lineMatch[1], 10);
            const lines = chart.split('\n');
            if (lines[lineNum - 1]) {
              errorText += `\n错误位置：第 ${lineNum} 行\n内容：${lines[lineNum - 1].trim()}`;
            }
          }
          
          // 检查常见问题
          if (errorMessage.includes('LINK_ID') || errorMessage.includes('Expecting')) {
            errorText += "\n提示：节点标签中包含特殊字符时，请使用引号括起来，例如：D1[\"主启动类\"]";
          }
          
          setError(errorText);
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

