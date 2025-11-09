/**
 * MermaidRenderer - Mermaid 图表渲染组件
 *
 * 用于在 Markdown 中渲染 Mermaid 图表
 * 支持流式输出场景，自动检测语法完整性
 *
 * @author Refinex Team
 */

import React, {useEffect, useRef, useState} from 'react';
import mermaid from 'mermaid';
import {Spin} from 'antd';

// 初始化 Mermaid 配置
mermaid.initialize({
  startOnLoad: false,
  theme: 'default',
  securityLevel: 'loose',
  fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial',
  flowchart: {
    useMaxWidth: true,
    htmlLabels: true,
    curve: 'basis',
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
  /** Mermaid 图表代码 */
  chart: string;
  /** 自定义类名 */
  className?: string;
}

/**
 * 检查 Mermaid 图表语法是否可能完整
 * 用于流式输出场景，避免渲染不完整的图表
 */
const isChartLikelyComplete = (chart: string): boolean => {
  if (!chart || chart.trim().length < 10) {
    return false;
  }

  const trimmed = chart.trim();
  const lines = trimmed.split('\n').filter(line => line.trim());

  // 至少需要 2 行（图表类型 + 至少一行内容）
  if (lines.length < 2) {
    return false;
  }

  // 检查是否有不完整的节点定义（以 [ 开头但没有 ] 结尾的行）
  const lastLine = lines[lines.length - 1].trim();

  // 检查最后一行是否有未闭合的括号
  const openBrackets = (lastLine.match(/\[/g) || []).length;
  const closeBrackets = (lastLine.match(/\]/g) || []).length;
  if (openBrackets > closeBrackets) {
    return false;
  }

  // 检查是否有未完成的箭头（以 --> 或 -> 结尾）
  if (lastLine.endsWith('-->') || lastLine.endsWith('->') || lastLine.endsWith('-')) {
    return false;
  }

  // 检查是否有未闭合的引号
  const quotes = (trimmed.match(/"/g) || []).length;
  if (quotes % 2 !== 0) {
    return false;
  }

  return true;
};

/**
 * Mermaid 图表渲染组件
 */
const MermaidRenderer: React.FC<MermaidRendererProps> = ({ chart, className }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const renderTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

  useEffect(() => {
    // 清除之前的定时器
    if (renderTimeoutRef.current) {
      clearTimeout(renderTimeoutRef.current);
    }

    const renderChart = async () => {
      if (!containerRef.current || !chart) {
        setLoading(false);
        return;
      }

      // 检查图表是否可能完整
      if (!isChartLikelyComplete(chart)) {
        setLoading(true);
        setError(null);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        // 生成唯一 ID
        const id = `mermaid-${Math.random().toString(36).substr(2, 9)}`;

        // 渲染图表
        const { svg } = await mermaid.render(id, chart);

        // 插入 SVG
        if (containerRef.current) {
          containerRef.current.innerHTML = svg;
        }

        setLoading(false);
      } catch (err) {
        // 静默处理错误，不显示 message（避免流式输出时频繁提示）
        console.warn('Mermaid 渲染失败（可能是流式输出中）:', err);

        // 如果图表看起来完整但渲染失败，才显示错误
        if (chart.trim().length > 50 && isChartLikelyComplete(chart)) {
          setError('图表语法错误，请检查 Mermaid 语法');
        }

        setLoading(false);
      }
    };

    // 延迟渲染，等待流式输出稳定
    renderTimeoutRef.current = setTimeout(renderChart, 300);

    return () => {
      if (renderTimeoutRef.current) {
        clearTimeout(renderTimeoutRef.current);
      }
    };
  }, [chart]);

  if (error) {
    return (
      <div className={`mermaid-error ${className || ''}`}>
        <div className="mermaid-error-content">
          <span className="mermaid-error-icon">⚠️</span>
          <span className="mermaid-error-text">{error}</span>
        </div>
        <pre className="mermaid-error-code">{chart}</pre>
      </div>
    );
  }

  return (
    <div className={`mermaid-container ${className || ''}`}>
      {loading && (
        <div className="mermaid-loading">
          <Spin tip="正在渲染图表..." />
        </div>
      )}
      <div ref={containerRef} className="mermaid-content" style={{ display: loading ? 'none' : 'block' }} />
    </div>
  );
};

export default MermaidRenderer;

