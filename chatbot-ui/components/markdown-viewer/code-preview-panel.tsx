/**
 * CodePreviewPanel - 代码预览面板组件
 *
 * 功能：
 * - 侧边滑出式面板
 * - Tab 切换（预览/源码）
 * - 工具栏（刷新、复制、下载、关闭）
 * - 响应式视图切换（可选）
 *
 * @author Refinex Team
 */

"use client";

import {useCallback, useEffect, useState} from 'react';
import {Sheet, SheetContent, SheetHeader, SheetTitle} from '@/components/ui/sheet';
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/ui/tooltip';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select';
import {CheckCircleFillIcon, CopyIcon, CrossIcon, DownloadIcon} from '@/components/icons';
import {LuEye} from 'react-icons/lu';
import {IoCodeSlash} from 'react-icons/io5';
import {toast} from 'sonner';
import {cn} from '@/lib/utils';
import CodeExecutor from './code-executor';
import type {CodePreviewPanelProps, DeviceInfo, ResponsiveViewType, ZoomLevel} from './types';
import {DEFAULT_ZOOM_LEVEL, DEVICE_PRESETS, getDeviceById, ZOOM_LEVELS} from './device-presets';

// 简单的代码高亮函数（使用转义）
const escapeHtml = (text: string): string => {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
};

const CodePreviewPanel: React.FC<CodePreviewPanelProps> = ({
  visible,
  code,
  language,
  codeType,
  onClose,
  title = '代码预览',
  layoutMode = 'sheet',
}) => {
  const [activeTab, setActiveTab] = useState<'preview' | 'source'>('preview');
  const [refreshKey, setRefreshKey] = useState(0);
  const [copied, setCopied] = useState(false);
  const [responsiveView, setResponsiveView] = useState<ResponsiveViewType>('desktop');
  const [selectedDevice, setSelectedDevice] = useState<string>('desktop');
  const [zoomLevel, setZoomLevel] = useState<ZoomLevel>(DEFAULT_ZOOM_LEVEL);
  const [highlightedCode, setHighlightedCode] = useState<string>('');

  // 获取当前设备信息
  const currentDevice: DeviceInfo | undefined = getDeviceById(selectedDevice);

  // 代码高亮 - 仅用于源码视图
  useEffect(() => {
    if (visible && code && activeTab === 'source') {
      // 简单转义，实际项目中可以使用 highlight.js
      setHighlightedCode(escapeHtml(code));
    }
  }, [visible, code, activeTab]);

  // 重置状态
  useEffect(() => {
    if (visible) {
      setActiveTab('preview');
      setRefreshKey(0);
      setCopied(false);
      setResponsiveView('desktop');
      setSelectedDevice('desktop');
      setZoomLevel(DEFAULT_ZOOM_LEVEL);
    }
  }, [visible]);

  // 当响应式视图类型改变时，自动选择对应类型的第一个设备
  useEffect(() => {
    const deviceOfType = DEVICE_PRESETS.find((d) => d.type === responsiveView);
    if (deviceOfType) {
      setSelectedDevice(deviceOfType.id);
    }
  }, [responsiveView]);

  // 当设备改变时，更新响应式视图类型
  const handleDeviceChange = useCallback((deviceId: string) => {
    setSelectedDevice(deviceId);
    const device = getDeviceById(deviceId);
    if (device) {
      setResponsiveView(device.type);
    }
  }, []);

  // 缩放控制
  const handleZoomIn = useCallback(() => {
    const currentIndex = ZOOM_LEVELS.indexOf(zoomLevel);
    if (currentIndex < ZOOM_LEVELS.length - 1) {
      setZoomLevel(ZOOM_LEVELS[currentIndex + 1]);
    }
  }, [zoomLevel]);

  const handleZoomOut = useCallback(() => {
    const currentIndex = ZOOM_LEVELS.indexOf(zoomLevel);
    if (currentIndex > 0) {
      setZoomLevel(ZOOM_LEVELS[currentIndex - 1]);
    }
  }, [zoomLevel]);

  // 刷新预览
  const handleRefresh = useCallback(() => {
    setRefreshKey(prev => prev + 1);
    toast.success('已刷新');
  }, []);

  // 复制代码
  const handleCopy = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      toast.success('复制成功');
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      toast.error('复制失败');
      console.error('复制失败:', err);
    }
  }, [code]);

  // 下载代码
  const handleDownload = useCallback(() => {
    try {
      const extensionMap: Record<string, string> = {
        html: 'html',
        react: 'jsx',
        vue: 'vue',
        svg: 'svg',
      };

      const ext = codeType ? extensionMap[codeType] : 'txt';
      const filename = `code-preview.${ext}`;

      const blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      toast.success('下载成功');
    } catch (err) {
      toast.error('下载失败');
      console.error('下载失败:', err);
    }
  }, [code, codeType]);

  // ESC 键关闭
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && visible) {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [visible, onClose]);

  // 渲染内容区域
  const renderContent = () => {
    return (
      <>
        {/* Tab 切换 */}
        <div className="flex items-center justify-between px-4 py-2 border-b shrink-0">
          <div className="flex items-center gap-1">
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  type="button"
                  onClick={() => setActiveTab('preview')}
                  className={cn(
                    'p-2 rounded-md transition-colors',
                    activeTab === 'preview'
                      ? 'bg-primary text-primary-foreground'
                      : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                  )}
                  aria-label="预览"
                >
                  <LuEye size={18} />
                </button>
              </TooltipTrigger>
              <TooltipContent>预览</TooltipContent>
            </Tooltip>
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  type="button"
                  onClick={() => setActiveTab('source')}
                  className={cn(
                    'p-2 rounded-md transition-colors',
                    activeTab === 'source'
                      ? 'bg-primary text-primary-foreground'
                      : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                  )}
                  aria-label="源码"
                >
                  <IoCodeSlash size={18} />
                </button>
              </TooltipTrigger>
              <TooltipContent>源码</TooltipContent>
            </Tooltip>
          </div>

          {/* 工具栏 */}
          <div className="flex items-center gap-2">
            {/* 响应式视图切换（仅预览模式） */}
            {activeTab === 'preview' && (
              <>
                {/* 视图类型切换 */}
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={() => setResponsiveView('desktop')}
                      className={cn(
                        'p-1.5 rounded-md transition-colors',
                        responsiveView === 'desktop'
                          ? 'bg-primary text-primary-foreground'
                          : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                      )}
                    >
                      <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2" />
                      </svg>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>桌面视图</TooltipContent>
                </Tooltip>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={() => setResponsiveView('tablet')}
                      className={cn(
                        'p-1.5 rounded-md transition-colors',
                        responsiveView === 'tablet'
                          ? 'bg-primary text-primary-foreground'
                          : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                      )}
                    >
                      <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
                      </svg>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>平板视图</TooltipContent>
                </Tooltip>

                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={() => setResponsiveView('mobile')}
                      className={cn(
                        'p-1.5 rounded-md transition-colors',
                        responsiveView === 'mobile'
                          ? 'bg-primary text-primary-foreground'
                          : 'text-muted-foreground hover:text-foreground hover:bg-accent'
                      )}
                    >
                      <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 18h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                      </svg>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>手机视图</TooltipContent>
                </Tooltip>

                {/* 设备选择器（非桌面视图时显示） */}
                {responsiveView !== 'desktop' && (
                  <Select value={selectedDevice} onValueChange={handleDeviceChange}>
                    <SelectTrigger className="w-[180px] h-8 text-xs">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {DEVICE_PRESETS.filter((d) => d.type === responsiveView).map((d) => (
                        <SelectItem key={d.id} value={d.id}>
                          {d.name} ({d.width}×{d.height})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}

                {/* 缩放控制（非桌面视图时显示） */}
                {responsiveView !== 'desktop' && (
                  <>
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <button
                          type="button"
                          onClick={handleZoomOut}
                          disabled={zoomLevel === ZOOM_LEVELS[0]}
                          className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent disabled:opacity-50 transition-colors"
                        >
                          <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM13 10H7" />
                          </svg>
                        </button>
                      </TooltipTrigger>
                      <TooltipContent>缩小</TooltipContent>
                    </Tooltip>

                    <Select
                      value={String(zoomLevel)}
                      onValueChange={(value) => setZoomLevel(Number(value) as ZoomLevel)}
                    >
                      <SelectTrigger className="w-[80px] h-8 text-xs">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {ZOOM_LEVELS.map((level) => (
                          <SelectItem key={level} value={String(level)}>
                            {level}%
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>

                    <Tooltip>
                      <TooltipTrigger asChild>
                        <button
                          type="button"
                          onClick={handleZoomIn}
                          disabled={zoomLevel === ZOOM_LEVELS[ZOOM_LEVELS.length - 1]}
                          className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent disabled:opacity-50 transition-colors"
                        >
                          <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v6m3-3H7" />
                          </svg>
                        </button>
                      </TooltipTrigger>
                      <TooltipContent>放大</TooltipContent>
                    </Tooltip>
                  </>
                )}

                {/* 刷新 */}
                <Tooltip>
                  <TooltipTrigger asChild>
                    <button
                      type="button"
                      onClick={handleRefresh}
                      className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    >
                      <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                      </svg>
                    </button>
                  </TooltipTrigger>
                  <TooltipContent>刷新</TooltipContent>
                </Tooltip>
              </>
            )}

            {/* 复制 */}
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  type="button"
                  onClick={handleCopy}
                  className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                >
                  {copied ? <CheckCircleFillIcon size={16} /> : <CopyIcon size={16} />}
                </button>
              </TooltipTrigger>
              <TooltipContent>{copied ? '已复制' : '复制代码'}</TooltipContent>
            </Tooltip>

            {/* 下载 */}
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  type="button"
                  onClick={handleDownload}
                  className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                >
                  <DownloadIcon size={16} />
                </button>
              </TooltipTrigger>
              <TooltipContent>下载代码</TooltipContent>
            </Tooltip>
          </div>
        </div>

        {/* Tab 内容区域 */}
        <div className="flex-1 overflow-hidden min-h-0">
          {/* 预览视图 */}
          {activeTab === 'preview' && (
            <div className="h-full overflow-auto bg-muted/30 p-4">
              <div
                className="mx-auto bg-background rounded-lg shadow-lg overflow-hidden"
                style={{
                  width:
                    responsiveView === 'desktop'
                      ? '100%'
                      : `${currentDevice ? currentDevice.width : 375}px`,
                  height:
                    responsiveView === 'desktop'
                      ? '100%'
                      : `${currentDevice ? currentDevice.height : 667}px`,
                  maxWidth: '100%',
                  maxHeight: responsiveView === 'desktop' ? '100%' : 'calc(100vh - 200px)',
                  transform: responsiveView !== 'desktop' ? `scale(${zoomLevel / 100})` : 'none',
                  transformOrigin: 'top center',
                }}
              >
                <CodeExecutor
                  key={refreshKey}
                  code={code}
                  codeType={codeType}
                  onError={(error) => {
                    console.error('Code execution error:', error);
                  }}
                />
              </div>
            </div>
          )}

          {/* 源码视图 */}
          {activeTab === 'source' && (
            <div className="h-full overflow-auto bg-[#1e1e1e] p-4">
              <pre className="text-sm font-mono text-[#d4d4d4]">
                <code
                  className={cn('language-', language)}
                  dangerouslySetInnerHTML={{ __html: highlightedCode || escapeHtml(code) }}
                />
              </pre>
            </div>
          )}
        </div>
      </>
    );
  };

  // 如果不显示，返回 null
  if (!visible) {
    return null;
  }

  // 分屏模式：直接渲染为固定布局面板
  if (layoutMode === 'split') {
    return (
      <div className="h-full flex flex-col bg-background border-l">
        <TooltipProvider>
          {/* 标题栏 */}
          <div className="flex items-center justify-between px-4 py-3 border-b bg-white dark:bg-background shrink-0">
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold">{title}</span>
              <span className="text-xs text-muted-foreground px-2 py-0.5 rounded bg-muted">
                {language.toUpperCase()}
              </span>
            </div>
            <div className="flex items-center gap-1">
              <Tooltip>
                <TooltipTrigger asChild>
                  <button
                    type="button"
                    onClick={handleRefresh}
                    className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    aria-label="刷新"
                  >
                    <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                  </button>
                </TooltipTrigger>
                <TooltipContent>刷新</TooltipContent>
              </Tooltip>
              <Tooltip>
                <TooltipTrigger asChild>
                  <button
                    type="button"
                    onClick={onClose}
                    className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    aria-label="关闭"
                  >
                    <CrossIcon size={16} />
                  </button>
                </TooltipTrigger>
                <TooltipContent>关闭</TooltipContent>
              </Tooltip>
            </div>
          </div>
          {renderContent()}
        </TooltipProvider>
      </div>
    );
  }

  // Sheet 模式（默认）
  return (
    <Sheet open={visible} onOpenChange={(open) => !open && onClose()}>
      <SheetContent
        side="right"
        className="w-full sm:w-[60%] lg:w-[800px] xl:w-[1000px] flex flex-col p-0 overflow-hidden"
      >
        <div className="flex-1 flex flex-col overflow-hidden">
          <SheetHeader className="px-4 py-3 border-b bg-muted/50">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <SheetTitle className="text-sm font-semibold">{title}</SheetTitle>
                <span className="text-xs text-muted-foreground px-2 py-0.5 rounded bg-muted">
                  {language.toUpperCase()}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <button
                        type="button"
                        onClick={handleRefresh}
                        className="p-1.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                        aria-label="刷新"
                      >
                        <svg className="size-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                      </button>
                    </TooltipTrigger>
                    <TooltipContent>刷新</TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              </div>
            </div>
          </SheetHeader>

          <TooltipProvider>
            {renderContent()}
          </TooltipProvider>
        </div>
      </SheetContent>
    </Sheet>
  );
};

export default CodePreviewPanel;

