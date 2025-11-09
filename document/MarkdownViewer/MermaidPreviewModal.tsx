/**
 * MermaidPreviewModal - Mermaid 图表大屏预览组件
 *
 * 功能：
 * - 全屏预览 Mermaid 图表
 * - 支持放大、缩小（按钮点击和鼠标滚轮）
 * - 支持旋转（90度递增）
 * - 支持下载（源码文件、PNG 图片、SVG 图片）
 * - 支持键盘快捷键（ESC 关闭、+/- 缩放、R 旋转）
 * - 支持拖拽移动
 *
 * @author Refinex Team
 */

import React, {useCallback, useEffect, useRef, useState} from 'react';
import type {MenuProps} from 'antd';
import {App, Button, Dropdown, Modal, Space, Tooltip} from 'antd';
import {
    CloseOutlined,
    DownloadOutlined,
    FullscreenExitOutlined,
    FullscreenOutlined,
    RedoOutlined,
    ZoomInOutlined,
    ZoomOutOutlined,
} from '@ant-design/icons';
import MermaidRenderer from './MermaidRenderer';
import './MermaidPreviewModal.less';

export interface MermaidPreviewModalProps {
  /** 是否显示 */
  visible: boolean;
  /** 关闭回调 */
  onClose: () => void;
  /** Mermaid 源码 */
  chart: string;
  /** 语言标识（用于下载文件名） */
  language?: string;
}

/**
 * Mermaid 图表大屏预览组件
 */
const MermaidPreviewModal: React.FC<MermaidPreviewModalProps> = ({
  visible,
  onClose,
  chart,
  language = 'mermaid',
}) => {
  // 使用 App 组件的 message API
  const { message } = App.useApp();

  // 缩放和旋转状态
  const [scale, setScale] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });

  // 容器引用
  const containerRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);

  // 缩放步长
  const ZOOM_STEP = 0.1;
  const MIN_SCALE = 0.1;
  const MAX_SCALE = 5;

  /**
   * 重置视图
   */
  const resetView = useCallback(() => {
    setScale(1);
    setRotation(0);
    setPosition({ x: 0, y: 0 });
  }, []);

  /**
   * 放大
   */
  const zoomIn = useCallback(() => {
    setScale((prev) => Math.min(prev + ZOOM_STEP, MAX_SCALE));
  }, []);

  /**
   * 缩小
   */
  const zoomOut = useCallback(() => {
    setScale((prev) => Math.max(prev - ZOOM_STEP, MIN_SCALE));
  }, []);

  /**
   * 旋转（90度递增）
   */
  const rotate = useCallback(() => {
    setRotation((prev) => (prev + 90) % 360);
  }, []);

  /**
   * 适应屏幕
   */
  const fitToScreen = useCallback(() => {
    if (!containerRef.current || !contentRef.current) return;

    const container = containerRef.current;
    const content = contentRef.current;

    // 获取容器尺寸
    const containerWidth = container.clientWidth;
    const containerHeight = container.clientHeight;

    // 获取 SVG 实际尺寸
    const svgElement = content.querySelector('svg');
    if (!svgElement) return;

    const bbox = svgElement.getBBox();
    const contentWidth = bbox.width + 48; // 加上 padding
    const contentHeight = bbox.height + 48;

    // 计算缩放比例，留 20% 边距以确保图表不会太小
    const scaleX = (containerWidth * 0.8) / contentWidth;
    const scaleY = (containerHeight * 0.8) / contentHeight;
    const newScale = Math.min(scaleX, scaleY, 1);

    setScale(newScale);
    setPosition({ x: 0, y: 0 });
  }, []);

  /**
   * 下载为源码文件
   */
  const downloadAsSource = useCallback(() => {
    try {
      const blob = new Blob([chart], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `mermaid-diagram.mmd`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      message.success('源码下载成功');
    } catch (err) {
      message.error('源码下载失败');
      console.error('下载失败:', err);
    }
  }, [chart]);

  /**
   * 下载为 SVG 图片
   */
  const downloadAsSVG = useCallback(() => {
    try {
      const svgElement = contentRef.current?.querySelector('svg');
      if (!svgElement) {
        message.error('未找到 SVG 元素');
        return;
      }

      // 克隆 SVG 并设置样式
      const clonedSvg = svgElement.cloneNode(true) as SVGElement;
      clonedSvg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');

      // 序列化 SVG
      const svgData = new XMLSerializer().serializeToString(clonedSvg);
      const blob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
      const url = URL.createObjectURL(blob);

      const link = document.createElement('a');
      link.href = url;
      link.download = `mermaid-diagram.svg`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      message.success('SVG 下载成功');
    } catch (err) {
      message.error('SVG 下载失败');
      console.error('下载失败:', err);
    }
  }, [message]);

  /**
   * 下载为 PNG 图片
   */
  const downloadAsPNG = useCallback(() => {
    try {
      const svgElement = contentRef.current?.querySelector('svg');
      if (!svgElement) {
        message.error('未找到 SVG 元素');
        return;
      }

      // 获取 SVG 尺寸
      const bbox = svgElement.getBBox();
      const width = bbox.width;
      const height = bbox.height;

      // 创建 canvas
      const canvas = document.createElement('canvas');
      const scale = 2; // 2倍分辨率，提高清晰度
      canvas.width = width * scale;
      canvas.height = height * scale;
      const ctx = canvas.getContext('2d', { willReadFrequently: false });

      if (!ctx) {
        message.error('无法创建 Canvas 上下文');
        return;
      }

      // 设置白色背景
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.scale(scale, scale);

      // 克隆 SVG 并处理所有图片元素
      const clonedSvg = svgElement.cloneNode(true) as SVGElement;
      clonedSvg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');

      // 处理 SVG 中的所有 image 元素，将外部图片转换为内联 data URL
      const images = clonedSvg.querySelectorAll('image');
      const imagePromises: Promise<void>[] = [];

      images.forEach((img) => {
        const href = img.getAttribute('href') || img.getAttribute('xlink:href');
        if (href && !href.startsWith('data:')) {
          // 如果是外部图片，尝试转换为 data URL
          const promise = new Promise<void>((resolve) => {
            const tempImg = new Image();
            tempImg.crossOrigin = 'anonymous';
            tempImg.onload = () => {
              try {
                const tempCanvas = document.createElement('canvas');
                tempCanvas.width = tempImg.width;
                tempCanvas.height = tempImg.height;
                const tempCtx = tempCanvas.getContext('2d');
                if (tempCtx) {
                  tempCtx.drawImage(tempImg, 0, 0);
                  const dataUrl = tempCanvas.toDataURL('image/png');
                  img.setAttribute('href', dataUrl);
                }
              } catch (e) {
                console.warn('无法转换图片:', href, e);
              }
              resolve();
            };
            tempImg.onerror = () => {
              console.warn('无法加载图片:', href);
              resolve();
            };
            tempImg.src = href;
          });
          imagePromises.push(promise);
        }
      });

      // 等待所有图片处理完成
      Promise.all(imagePromises).then(() => {
        // 序列化 SVG
        const svgData = new XMLSerializer().serializeToString(clonedSvg);
        const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
        const url = URL.createObjectURL(svgBlob);

        // 创建图片
        const img = new Image();
        img.onload = () => {
          ctx.drawImage(img, 0, 0);
          URL.revokeObjectURL(url);

          // 转换为 PNG 并下载
          canvas.toBlob((blob) => {
            if (blob) {
              const pngUrl = URL.createObjectURL(blob);
              const link = document.createElement('a');
              link.href = pngUrl;
              link.download = `mermaid-diagram.png`;
              document.body.appendChild(link);
              link.click();
              document.body.removeChild(link);
              URL.revokeObjectURL(pngUrl);
              message.success('PNG 下载成功');
            } else {
              message.error('PNG 生成失败');
            }
          }, 'image/png');
        };

        img.onerror = () => {
          URL.revokeObjectURL(url);
          message.error('图片加载失败');
        };

        img.src = url;
      });
    } catch (err) {
      message.error('PNG 下载失败');
      console.error('下载失败:', err);
    }
  }, [message]);

  /**
   * 下载菜单点击处理
   */
  const handleDownloadMenuClick: MenuProps['onClick'] = useCallback(({ key }: { key: string }) => {
    switch (key) {
      case 'source':
        downloadAsSource();
        break;
      case 'svg':
        downloadAsSVG();
        break;
      case 'png':
        downloadAsPNG();
        break;
    }
  }, [downloadAsSource, downloadAsSVG, downloadAsPNG]);

  /**
   * 下载菜单
   */
  const downloadMenuItems: MenuProps['items'] = [
    {
      key: 'source',
      label: '下载源码 (.mmd)',
    },
    {
      key: 'svg',
      label: '下载 SVG 图片',
    },
    {
      key: 'png',
      label: '下载 PNG 图片',
    },
  ];

  /**
   * 鼠标滚轮缩放
   */
  const handleWheel = useCallback((e: WheelEvent) => {
    e.preventDefault();
    const delta = e.deltaY > 0 ? -ZOOM_STEP : ZOOM_STEP;
    setScale((prev) => Math.max(MIN_SCALE, Math.min(MAX_SCALE, prev + delta)));
  }, []);

  /**
   * 鼠标拖拽开始
   */
  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    if (e.button === 0) {
      // 左键
      setIsDragging(true);
      setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y });
    }
  }, [position]);

  /**
   * 鼠标拖拽移动
   */
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

  /**
   * 鼠标拖拽结束
   */
  const handleMouseUp = useCallback(() => {
    setIsDragging(false);
  }, []);

  /**
   * 键盘快捷键
   */
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      switch (e.key) {
        case 'Escape':
          onClose();
          break;
        case '+':
        case '=':
          e.preventDefault();
          zoomIn();
          break;
        case '-':
        case '_':
          e.preventDefault();
          zoomOut();
          break;
        case 'r':
        case 'R':
          e.preventDefault();
          rotate();
          break;
        case '0':
          e.preventDefault();
          resetView();
          break;
        case 'f':
        case 'F':
          e.preventDefault();
          fitToScreen();
          break;
        default:
          break;
      }
    },
    [onClose, zoomIn, zoomOut, rotate, resetView, fitToScreen]
  );

  /**
   * 监听事件
   */
  useEffect(() => {
    if (!visible) return;

    const container = containerRef.current;
    if (container) {
      container.addEventListener('wheel', handleWheel, { passive: false });
    }

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      if (container) {
        container.removeEventListener('wheel', handleWheel);
      }
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [visible, handleWheel, handleMouseMove, handleMouseUp, handleKeyDown]);

  /**
   * 打开时重置视图并适应屏幕
   */
  useEffect(() => {
    if (visible) {
      resetView();
      // 延迟执行适应屏幕，等待渲染完成
      setTimeout(() => {
        fitToScreen();
      }, 200);
    }
  }, [visible, resetView, fitToScreen]);

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      footer={null}
      width="100vw"
      style={{ top: 0, maxWidth: '100vw', padding: 0 }}
      styles={{
        body: { height: 'calc(100vh - 110px)', padding: 0 },
      }}
      className="mermaid-preview-modal"
      destroyOnHidden
      closeIcon={false}
    >
      {/* 工具栏 */}
      <div className="mermaid-preview-toolbar">
        <Space size="small">
          {/* 缩放信息 */}
          <span className="mermaid-preview-scale-info">{Math.round(scale * 100)}%</span>

          {/* 放大 */}
          <Tooltip title="放大 (+)">
            <Button
              type="text"
              icon={<ZoomInOutlined />}
              onClick={zoomIn}
              disabled={scale >= MAX_SCALE}
            />
          </Tooltip>

          {/* 缩小 */}
          <Tooltip title="缩小 (-)">
            <Button
              type="text"
              icon={<ZoomOutOutlined />}
              onClick={zoomOut}
              disabled={scale <= MIN_SCALE}
            />
          </Tooltip>

          {/* 适应屏幕 */}
          <Tooltip title="适应屏幕 (F)">
            <Button type="text" icon={<FullscreenOutlined />} onClick={fitToScreen} />
          </Tooltip>

          {/* 实际大小 */}
          <Tooltip title="实际大小 (0)">
            <Button type="text" icon={<FullscreenExitOutlined />} onClick={resetView} />
          </Tooltip>

          {/* 旋转 */}
          <Tooltip title="旋转 90° (R)">
            <Button type="text" icon={<RedoOutlined />} onClick={rotate} />
          </Tooltip>

          {/* 下载 */}
          <Dropdown menu={{ items: downloadMenuItems, onClick: handleDownloadMenuClick }} placement="bottomLeft">
            <Tooltip title="下载">
              <Button type="text" icon={<DownloadOutlined />} />
            </Tooltip>
          </Dropdown>
        </Space>

        {/* 快捷键提示和关闭按钮 */}
        <div className="mermaid-preview-toolbar-right">
          <span className="mermaid-preview-shortcuts">
            快捷键：ESC 关闭 | +/- 缩放 | R 旋转 | F 适应屏幕 | 0 实际大小 | 鼠标滚轮缩放 | 拖拽移动
          </span>
          <Tooltip title="关闭 (ESC)">
            <Button type="text" icon={<CloseOutlined />} onClick={onClose} />
          </Tooltip>
        </div>
      </div>

      {/* 预览容器 */}
      <div
        ref={containerRef}
        className="mermaid-preview-container"
        onMouseDown={handleMouseDown}
        style={{
          cursor: isDragging ? 'grabbing' : 'grab',
        }}
      >
        <div
          ref={contentRef}
          className="mermaid-preview-content"
          style={{
            transform: `translate(${position.x}px, ${position.y}px) scale(${scale}) rotate(${rotation}deg)`,
            transformOrigin: 'center center',
            transition: isDragging ? 'none' : 'transform 0.2s ease-out',
          }}
        >
          <MermaidRenderer chart={chart} className="mermaid-preview-diagram" />
        </div>
      </div>
    </Modal>
  );
};

export default MermaidPreviewModal;

