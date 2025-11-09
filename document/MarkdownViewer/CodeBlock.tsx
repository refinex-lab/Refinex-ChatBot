/**
 * CodeBlock - 增强的代码块组件
 *
 * 功能：
 * - 左上角显示语言标签
 * - 右上角显示操作按钮（复制、下载）
 * - Mermaid 图表额外显示"查看源码"按钮
 * - 代码高亮支持
 *
 * @author Refinex Team
 */

import React, {useMemo, useState} from 'react';
import {Button, message, Tooltip} from 'antd';
import {
    CheckOutlined,
    CodeOutlined,
    CopyOutlined,
    DownloadOutlined,
    ExpandOutlined,
    ThunderboltOutlined,
} from '@ant-design/icons';
import MermaidRenderer from './MermaidRenderer';
import MermaidPreviewModal from './MermaidPreviewModal';
import {CodePreviewPanel} from '../CodePreview';
import {detectExecutableCode} from '../CodePreview/utils/codeDetector';
import type {CodeType} from '../CodePreview/types';

export interface CodeBlockProps {
  /** 代码内容（字符串或已高亮的 React 元素） */
  code: string | React.ReactNode;
  /** 编程语言 */
  language?: string;
  /** 是否为行内代码 */
  inline?: boolean;
  /** 自定义类名 */
  className?: string;
  /** 是否启用 Mermaid */
  enableMermaid?: boolean;
}

/**
 * 增强的代码块组件
 */
const CodeBlock: React.FC<CodeBlockProps> = ({
  code,
  language = 'text',
  inline = false,
  className = '',
  enableMermaid = true,
}) => {
  const [copied, setCopied] = useState(false);
  const [showSource, setShowSource] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(false);
  const [codePreviewVisible, setCodePreviewVisible] = useState(false);

  // 行内代码直接返回
  if (inline) {
    return <code className={className}>{code}</code>;
  }

  // 判断是否为 Mermaid 图表
  const isMermaid = enableMermaid && language === 'mermaid';

  // 提取纯文本代码（用于复制和下载）
  const getPlainTextCode = (): string => {
    if (typeof code === 'string') {
      return code;
    }
    // 如果是数组，递归提取每个元素的文本
    if (Array.isArray(code)) {
      return extractTextFromReactElement(code);
    }
    // 如果是 React 元素，尝试提取文本内容
    if (React.isValidElement(code)) {
      return extractTextFromReactElement(code);
    }
    return String(code);
  };

  /**
   * 从 React 元素中提取纯文本
   */
  const extractTextFromReactElement = (element: any): string => {
    // 处理字符串
    if (typeof element === 'string') {
      return element;
    }
    // 处理数字
    if (typeof element === 'number') {
      return String(element);
    }
    // 处理数组
    if (Array.isArray(element)) {
      return element.map(extractTextFromReactElement).join('');
    }
    // 处理 React 元素
    if (React.isValidElement(element)) {
      const props = element.props as any;
      if (props && props.children) {
        return extractTextFromReactElement(props.children);
      }
      // React 元素没有 children，返回空字符串
      return '';
    }
    // 处理其他类型（null, undefined, boolean 等）
    if (element == null || typeof element === 'boolean') {
      return '';
    }
    // 处理对象类型：返回空字符串，避免 "[object Object]"
    if (typeof element === 'object') {
      return '';
    }
    // 最后尝试转换为字符串
    return String(element);
  };

  // 检测代码是否可执行
  const codeDetection = useMemo(() => {
    const plainText = getPlainTextCode();
    return detectExecutableCode(plainText, language);
  }, [code, language]);

  const isExecutable = codeDetection.executable;
  const executableType: CodeType = codeDetection.type;

  /**
   * 复制代码到剪贴板
   */
  const handleCopy = async () => {
    try {
      const plainText = getPlainTextCode();
      await navigator.clipboard.writeText(plainText);
      setCopied(true);
      message.success('复制成功');
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      message.error('复制失败');
      console.error('复制失败:', err);
    }
  };

  /**
   * 下载代码为文件
   */
  const handleDownload = () => {
    try {
      const plainText = getPlainTextCode();

      // 根据语言确定文件扩展名
      const extensionMap: Record<string, string> = {
        javascript: 'js',
        typescript: 'ts',
        python: 'py',
        java: 'java',
        cpp: 'cpp',
        csharp: 'cs',
        go: 'go',
        rust: 'rs',
        php: 'php',
        ruby: 'rb',
        swift: 'swift',
        kotlin: 'kt',
        scala: 'scala',
        html: 'html',
        css: 'css',
        scss: 'scss',
        less: 'less',
        json: 'json',
        xml: 'xml',
        yaml: 'yaml',
        markdown: 'md',
        sql: 'sql',
        shell: 'sh',
        bash: 'sh',
        powershell: 'ps1',
        mermaid: 'mmd',
      };

      const ext = extensionMap[language.toLowerCase()] || 'txt';
      const filename = `code.${ext}`;

      // 创建 Blob 并下载
      const blob = new Blob([plainText], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      message.success('下载成功');
    } catch (err) {
      message.error('下载失败');
      console.error('下载失败:', err);
    }
  };

  /**
   * 切换源码显示
   */
  const handleToggleSource = () => {
    setShowSource(!showSource);
  };

  /**
   * 打开大屏预览
   */
  const handleOpenPreview = () => {
    setPreviewVisible(true);
  };

  /**
   * 关闭大屏预览
   */
  const handleClosePreview = () => {
    setPreviewVisible(false);
  };

  /**
   * 打开代码预览
   */
  const handleExecuteCode = () => {
    setCodePreviewVisible(true);
  };

  /**
   * 关闭代码预览
   */
  const handleCloseCodePreview = () => {
    setCodePreviewVisible(false);
  };

  /**
   * 获取语言显示名称
   */
  const getLanguageDisplayName = (lang: string): string => {
    const nameMap: Record<string, string> = {
      javascript: 'JavaScript',
      typescript: 'TypeScript',
      python: 'Python',
      java: 'Java',
      cpp: 'C++',
      csharp: 'C#',
      go: 'Go',
      rust: 'Rust',
      php: 'PHP',
      ruby: 'Ruby',
      swift: 'Swift',
      kotlin: 'Kotlin',
      scala: 'Scala',
      html: 'HTML',
      css: 'CSS',
      scss: 'SCSS',
      less: 'Less',
      json: 'JSON',
      xml: 'XML',
      yaml: 'YAML',
      markdown: 'Markdown',
      sql: 'SQL',
      shell: 'Shell',
      bash: 'Bash',
      powershell: 'PowerShell',
      mermaid: 'Mermaid',
      text: 'Text',
    };

    return nameMap[lang.toLowerCase()] || lang.toUpperCase();
  };

  return (
    <>
      <div className="code-block-enhanced">
        {/* 顶部工具栏 */}
        <div className="code-block-toolbar">
          {/* 左侧：语言标签 */}
          <div className="code-block-language">
            {getLanguageDisplayName(language)}
          </div>

          {/* 右侧：操作按钮 */}
          <div className="code-block-actions">
            {/* 可执行代码：执行按钮 */}
            {isExecutable && !isMermaid && (
              <Tooltip title="运行代码">
                <Button
                  type="text"
                  size="small"
                  icon={<ThunderboltOutlined />}
                  onClick={handleExecuteCode}
                  className="code-block-action-btn"
                />
              </Tooltip>
            )}

            {/* Mermaid 图表：大屏预览按钮 */}
            {isMermaid && (
              <Tooltip title="大屏预览">
                <Button
                  type="text"
                  size="small"
                  icon={<ExpandOutlined />}
                  onClick={handleOpenPreview}
                  className="code-block-action-btn"
                />
              </Tooltip>
            )}

            {/* Mermaid 图表：查看源码按钮 */}
            {isMermaid && (
              <Tooltip title={showSource ? '查看图表' : '查看源码'}>
                <Button
                  type="text"
                  size="small"
                  icon={<CodeOutlined />}
                  onClick={handleToggleSource}
                  className="code-block-action-btn"
                />
              </Tooltip>
            )}

            {/* 复制按钮 */}
            <Tooltip title={copied ? '已复制' : '复制代码'}>
              <Button
                type="text"
                size="small"
                icon={copied ? <CheckOutlined /> : <CopyOutlined />}
                onClick={handleCopy}
                className="code-block-action-btn"
              />
            </Tooltip>

            {/* 下载按钮 */}
            <Tooltip title="下载代码">
              <Button
                type="text"
                size="small"
                icon={<DownloadOutlined />}
                onClick={handleDownload}
                className="code-block-action-btn"
              />
            </Tooltip>
          </div>
        </div>

        {/* 代码内容区域 */}
        <div className="code-block-content">
          {isMermaid && !showSource ? (
            // Mermaid 图表渲染
            <MermaidRenderer chart={getPlainTextCode()} className="code-block-mermaid" />
          ) : (
            // 普通代码块 - 直接使用 rehype-highlight 处理后的 React 元素
            <pre className="code-block-pre">
              <code className={`hljs language-${language}`}>
                {code}
              </code>
            </pre>
          )}
        </div>
      </div>

      {/* Mermaid 大屏预览 Modal */}
      {isMermaid && (
        <MermaidPreviewModal
          visible={previewVisible}
          onClose={handleClosePreview}
          chart={getPlainTextCode()}
          language={language}
        />
      )}

      {/* 代码预览面板 */}
      {isExecutable && executableType && (
        <CodePreviewPanel
          visible={codePreviewVisible}
          code={getPlainTextCode()}
          language={language}
          codeType={executableType}
          onClose={handleCloseCodePreview}
          title={`${getLanguageDisplayName(language)} 代码预览`}
        />
      )}
    </>
  );
};

export default CodeBlock;

