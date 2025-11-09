/**
 * CodePreview - 类型定义
 *
 * @author Refinex Team
 */

/**
 * 可执行代码类型
 */
export type CodeType = 'html' | 'react' | 'vue' | 'svg' | null;

/**
 * 代码检测结果
 */
export interface CodeDetectionResult {
  /** 是否可执行 */
  executable: boolean;
  /** 代码类型 */
  type: CodeType;
  /** 置信度 (0-1) */
  confidence: number;
  /** 检测到的特征 */
  features?: string[];
}

/**
 * 代码执行配置
 */
export interface CodeExecutionConfig {
  /** 代码内容 */
  code: string;
  /** 代码类型 */
  type: CodeType;
  /** 是否启用控制台日志 */
  enableConsole?: boolean;
  /** 超时时间（毫秒） */
  timeout?: number;
  /** 自定义 CDN 地址 */
  cdnUrls?: {
    react?: string;
    reactDOM?: string;
    babel?: string;
    vue?: string;
  };
}

/**
 * 代码执行结果
 */
export interface CodeExecutionResult {
  /** 是否成功 */
  success: boolean;
  /** 可执行的 HTML 内容 */
  html?: string;
  /** 错误信息 */
  error?: {
    message: string;
    stack?: string;
    line?: number;
    column?: number;
  };
  /** 控制台输出 */
  console?: string[];
}

/**
 * 预览面板配置
 */
export interface CodePreviewPanelProps {
  /** 是否显示 */
  visible: boolean;
  /** 源代码 */
  code: string;
  /** 编程语言 */
  language: string;
  /** 代码类型 */
  codeType: CodeType;
  /** 关闭回调 */
  onClose: () => void;
  /** 标题（可选） */
  title?: string;
}

/**
 * 代码执行器配置
 */
export interface CodeExecutorProps {
  /** 要执行的代码 */
  code: string;
  /** 代码类型 */
  codeType: CodeType;
  /** 执行配置 */
  config?: Partial<CodeExecutionConfig>;
  /** 错误回调 */
  onError?: (error: Error) => void;
  /** 加载完成回调 */
  onLoad?: () => void;
  /** 自定义类名 */
  className?: string;
}

/**
 * 响应式视图类型
 */
export type ResponsiveViewType = 'desktop' | 'tablet' | 'mobile';

/**
 * 设备信息
 */
export interface DeviceInfo {
  /** 设备唯一标识 */
  id: string;
  /** 设备名称 */
  name: string;
  /** 设备类型 */
  type: ResponsiveViewType;
  /** 宽度（像素） */
  width: number;
  /** 高度（像素） */
  height: number;
  /** 设备像素比 */
  pixelRatio?: number;
  /** 用户代理字符串 */
  userAgent?: string;
}

/**
 * 缩放比例类型
 */
export type ZoomLevel = 25 | 50 | 75 | 100 | 125 | 150;

/**
 * 响应式视图配置
 */
export interface ResponsiveViewConfig {
  type: ResponsiveViewType;
  width: number;
  height: number;
  label: string;
  icon?: React.ReactNode;
}

/**
 * 工具栏操作类型
 */
export type ToolbarAction = 'refresh' | 'copy' | 'download' | 'responsive' | 'close';

/**
 * 工具栏配置
 */
export interface ToolbarConfig {
  /** 是否显示刷新按钮 */
  showRefresh?: boolean;
  /** 是否显示复制按钮 */
  showCopy?: boolean;
  /** 是否显示下载按钮 */
  showDownload?: boolean;
  /** 是否显示响应式切换 */
  showResponsive?: boolean;
  /** 自定义操作 */
  customActions?: Array<{
    key: string;
    label: string;
    icon: React.ReactNode;
    onClick: () => void;
  }>;
}

