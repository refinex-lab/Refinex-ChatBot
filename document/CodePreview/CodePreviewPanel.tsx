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

import React, {useCallback, useEffect, useState} from 'react';
import {App, Button, Select, Tooltip} from 'antd';
import {
    CheckOutlined,
    CloseOutlined,
    CopyOutlined,
    DesktopOutlined,
    DownloadOutlined,
    MobileOutlined,
    ReloadOutlined,
    TabletOutlined,
    ZoomInOutlined,
    ZoomOutOutlined,
} from '@ant-design/icons';
import hljs from 'highlight.js';
import CodeExecutor from './CodeExecutor';
import type {CodePreviewPanelProps, DeviceInfo, ResponsiveViewType, ZoomLevel} from './types';
import {DEFAULT_ZOOM_LEVEL, DEVICE_PRESETS, getDeviceById, ZOOM_LEVELS} from './devicePresets';
import './index.less';
import 'highlight.js/styles/github.css';

const CodePreviewPanel: React.FC<CodePreviewPanelProps> = ({
  visible,
  code,
  language,
  codeType,
  onClose,
  title = '代码预览',
}) => {
  const { message } = App.useApp();
  const [activeTab, setActiveTab] = useState<'preview' | 'source'>('preview');
  const [refreshKey, setRefreshKey] = useState(0);
  const [copied, setCopied] = useState(false);
  const [responsiveView, setResponsiveView] = useState<ResponsiveViewType>('desktop');
  const [selectedDevice, setSelectedDevice] = useState<string>('desktop'); // 当前选中的设备 ID
  const [zoomLevel, setZoomLevel] = useState<ZoomLevel>(DEFAULT_ZOOM_LEVEL); // 缩放比例
  const [closing, setClosing] = useState(false);
  const [highlightedCode, setHighlightedCode] = useState<string>('');

  // 获取当前设备信息
  const currentDevice: DeviceInfo | undefined = getDeviceById(selectedDevice);

  // HTML 转义函数 - 防止代码被当作 HTML 执行
  const escapeHtml = useCallback((text: string): string => {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }, []);

  // 代码高亮 - 仅用于源码视图
  useEffect(() => {
    if (visible && code && activeTab === 'source') {
      try {
        // 使用 highlight.js 进行语法高亮
        // highlight.js 会自动转义 HTML 特殊字符，所以 result.value 是安全的
        const result = hljs.highlight(code, { language: language || 'plaintext' });
        setHighlightedCode(result.value);
      } catch (error) {
        console.error('代码高亮失败:', error);
        // 如果高亮失败，手动转义 HTML 并使用原始代码
        setHighlightedCode(escapeHtml(code));
      }
    }
  }, [visible, code, language, activeTab, escapeHtml]);

  // 重置状态
  useEffect(() => {
    if (visible) {
      setActiveTab('preview');
      setRefreshKey(0);
      setCopied(false);
      setResponsiveView('desktop');
      setSelectedDevice('desktop');
      setZoomLevel(DEFAULT_ZOOM_LEVEL);
      setClosing(false);
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
    message.success('已刷新');
  }, [message]);

  // 复制代码
  const handleCopy = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      message.success('复制成功');
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      message.error('复制失败');
      console.error('复制失败:', err);
    }
  }, [code, message]);

  // 下载代码
  const handleDownload = useCallback(() => {
    try {
      // 根据代码类型确定文件扩展名
      const extensionMap: Record<string, string> = {
        html: 'html',
        react: 'jsx',
        vue: 'vue',
        svg: 'svg',
      };

      const ext = codeType ? extensionMap[codeType] : 'txt';
      const filename = `code-preview.${ext}`;

      // 创建 Blob 并下载
      const blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
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
  }, [code, codeType, message]);

  // 关闭面板（带动画）
  const handleClose = useCallback(() => {
    setClosing(true);
    setTimeout(() => {
      onClose();
      setClosing(false);
    }, 300); // 与 CSS 动画时间一致
  }, [onClose]);

  // ESC 键关闭
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && visible) {
        handleClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [visible, handleClose]);

  if (!visible) {
    return null;
  }

  return (
    <>
      {/* 遮罩层 */}
      <div
        className={`code-preview-overlay ${closing ? 'closing' : ''}`}
        onClick={handleClose}
      />

      {/* 预览面板 */}
      <div className={`code-preview-panel ${closing ? 'closing' : ''}`}>
        {/* 标题栏 */}
        <div className="code-preview-header">
          <div className="code-preview-title">
            <span>{title}</span>
            <span className="code-preview-language">{language.toUpperCase()}</span>
          </div>
          <Button
            type="text"
            size="small"
            icon={<CloseOutlined />}
            onClick={handleClose}
            className="code-preview-close-btn"
          />
        </div>

        {/* Tab 切换 */}
        <div className="code-preview-tabs">
          <div className="code-preview-tab-list">
            <button
              className={`code-preview-tab ${activeTab === 'preview' ? 'active' : ''}`}
              onClick={() => setActiveTab('preview')}
            >
              预览
            </button>
            <button
              className={`code-preview-tab ${activeTab === 'source' ? 'active' : ''}`}
              onClick={() => setActiveTab('source')}
            >
              源码
            </button>
          </div>

          {/* 工具栏 */}
          <div className="code-preview-actions">
            {/* 响应式视图切换（仅预览模式） */}
            {activeTab === 'preview' && (
              <>
                {/* 视图类型切换 */}
                <Tooltip title="桌面视图">
                  <Button
                    type={responsiveView === 'desktop' ? 'primary' : 'text'}
                    size="small"
                    icon={<DesktopOutlined />}
                    onClick={() => setResponsiveView('desktop')}
                  />
                </Tooltip>
                <Tooltip title="平板视图">
                  <Button
                    type={responsiveView === 'tablet' ? 'primary' : 'text'}
                    size="small"
                    icon={<TabletOutlined />}
                    onClick={() => setResponsiveView('tablet')}
                  />
                </Tooltip>
                <Tooltip title="手机视图">
                  <Button
                    type={responsiveView === 'mobile' ? 'primary' : 'text'}
                    size="small"
                    icon={<MobileOutlined />}
                    onClick={() => setResponsiveView('mobile')}
                  />
                </Tooltip>

                {/* 设备选择器（非桌面视图时显示） */}
                {responsiveView !== 'desktop' && (
                  <Select
                    size="small"
                    value={selectedDevice}
                    onChange={handleDeviceChange}
                    style={{ width: 180 }}
                    options={DEVICE_PRESETS.filter((d) => d.type === responsiveView).map((d) => ({
                      label: `${d.name} (${d.width}×${d.height})`,
                      value: d.id,
                    }))}
                  />
                )}

                {/* 缩放控制（非桌面视图时显示） */}
                {responsiveView !== 'desktop' && (
                  <>
                    <Tooltip title="缩小">
                      <Button
                        type="text"
                        size="small"
                        icon={<ZoomOutOutlined />}
                        onClick={handleZoomOut}
                        disabled={zoomLevel === ZOOM_LEVELS[0]}
                      />
                    </Tooltip>
                    <Select
                      size="small"
                      value={zoomLevel}
                      onChange={(value) => setZoomLevel(value as ZoomLevel)}
                      style={{ width: 80 }}
                      options={ZOOM_LEVELS.map((level) => ({
                        label: `${level}%`,
                        value: level,
                      }))}
                    />
                    <Tooltip title="放大">
                      <Button
                        type="text"
                        size="small"
                        icon={<ZoomInOutlined />}
                        onClick={handleZoomIn}
                        disabled={zoomLevel === ZOOM_LEVELS[ZOOM_LEVELS.length - 1]}
                      />
                    </Tooltip>
                  </>
                )}

                {/* 刷新 */}
                <Tooltip title="刷新">
                  <Button
                    type="text"
                    size="small"
                    icon={<ReloadOutlined />}
                    onClick={handleRefresh}
                  />
                </Tooltip>
              </>
            )}

            {/* 复制 */}
            <Tooltip title={copied ? '已复制' : '复制代码'}>
              <Button
                type="text"
                size="small"
                icon={copied ? <CheckOutlined /> : <CopyOutlined />}
                onClick={handleCopy}
              />
            </Tooltip>

            {/* 下载 */}
            <Tooltip title="下载代码">
              <Button
                type="text"
                size="small"
                icon={<DownloadOutlined />}
                onClick={handleDownload}
              />
            </Tooltip>
          </div>
        </div>

        {/* Tab 内容区域 */}
        <div className="code-preview-content">
          {/* 预览视图 */}
          {activeTab === 'preview' && (
            <div className="code-preview-viewport">
              {/* 设备信息提示（非桌面视图时显示） */}
              {/* {responsiveView !== 'desktop' && currentDevice && (
                <div className="code-preview-device-info">
                  <span className="device-name">{currentDevice.name}</span>
                  <span className="device-size">
                    {currentDevice.width} × {currentDevice.height}
                  </span>
                  <span className="device-zoom">{zoomLevel}%</span>
                </div>
              )} */}

              <div
                className="code-preview-frame"
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
                  maxHeight: '100%',
                  margin: responsiveView !== 'desktop' ? '0 auto' : '0',
                  boxShadow:
                    responsiveView !== 'desktop' ? '0 4px 12px rgba(0, 0, 0, 0.15)' : 'none',
                  border: responsiveView !== 'desktop' ? '1px solid #e8e8e8' : 'none',
                  borderRadius: responsiveView !== 'desktop' ? '8px' : '0',
                  overflow: 'hidden',
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
            <div className="code-preview-source">
              <pre className="code-preview-source-pre">
                <code
                  className={`hljs language-${language}`}
                  dangerouslySetInnerHTML={{ __html: highlightedCode || code }}
                />
              </pre>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default CodePreviewPanel;

