/**
 * CodeExecutor - 代码执行器组件
 *
 * 功能：
 * - 在 iframe 沙箱中安全执行代码
 * - 捕获错误和控制台输出
 * - 支持加载状态和错误显示
 *
 * @author Refinex Team
 */

import React, {useEffect, useRef, useState} from 'react';
import {Alert, Spin} from 'antd';
import {buildExecutableHTML, SANDBOX_ATTRIBUTES} from './utils/sandboxBuilder';
import type {CodeExecutorProps} from './types';

const CodeExecutor: React.FC<CodeExecutorProps> = ({
  code,
  codeType,
  config,
  onError,
  onLoad,
  className = '',
}) => {
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [consoleOutput, setConsoleOutput] = useState<string[]>([]);

  useEffect(() => {
    if (!code || !codeType) {
      setError('无效的代码或代码类型');
      setLoading(false);
      return;
    }

    // 重置状态
    setLoading(true);
    setError(null);
    setConsoleOutput([]);

    // 构建可执行的 HTML
    const result = buildExecutableHTML({
      code,
      type: codeType,
      enableConsole: true,
      ...config,
    });

    if (!result.success) {
      setError(result.error?.message || '代码构建失败');
      setLoading(false);
      onError?.(new Error(result.error?.message));
      return;
    }

    // 注入到 iframe
    if (iframeRef.current && result.html) {
      try {
        iframeRef.current.srcdoc = result.html;
      } catch (err) {
        const errorMsg = err instanceof Error ? err.message : '代码注入失败';
        setError(errorMsg);
        setLoading(false);
        onError?.(new Error(errorMsg));
      }
    }
  }, [code, codeType, config, onError]);

  // 监听 iframe 消息
  useEffect(() => {
    const handleMessage = (event: MessageEvent) => {
      if (event.data && typeof event.data === 'object') {
        const { type, message: msg, method, args } = event.data;

        // 错误消息
        if (type === 'error') {
          const errorMsg = msg || '代码执行错误';
          setError(errorMsg);
          onError?.(new Error(errorMsg));
        }

        // 控制台输出
        if (type === 'console') {
          setConsoleOutput(prev => [...prev, `[${method}] ${args?.join(' ')}`]);
        }
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [onError]);

  // iframe 加载完成
  const handleIframeLoad = () => {
    setLoading(false);
    onLoad?.();
  };

  // iframe 加载错误
  const handleIframeError = () => {
    setError('iframe 加载失败');
    setLoading(false);
    onError?.(new Error('iframe 加载失败'));
  };

  return (
    <div className={`code-executor ${className}`} style={{ position: 'relative', width: '100%', height: '100%' }}>
      {/* 加载状态 */}
      {loading && (
        <div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'rgba(255, 255, 255, 0.9)',
            zIndex: 10,
          }}
        >
          <Spin tip="正在加载..." />
        </div>
      )}

      {/* 错误提示 */}
      {error && !loading && (
        <div style={{ padding: '16px' }}>
          <Alert
            message="执行错误"
            description={error}
            type="error"
            showIcon
            closable
            onClose={() => setError(null)}
          />
        </div>
      )}

      {/* 控制台输出（开发模式） */}
      {process.env.NODE_ENV === 'development' && consoleOutput.length > 0 && (
        <div
          style={{
            position: 'absolute',
            bottom: 0,
            left: 0,
            right: 0,
            maxHeight: '200px',
            overflow: 'auto',
            background: '#1e1e1e',
            color: '#d4d4d4',
            padding: '8px',
            fontSize: '12px',
            fontFamily: 'monospace',
            zIndex: 5,
            borderTop: '1px solid #333',
          }}
        >
          {consoleOutput.map((output, index) => (
            <div key={index} style={{ padding: '2px 0' }}>
              {output}
            </div>
          ))}
        </div>
      )}

      {/* iframe 沙箱 */}
      <iframe
        ref={iframeRef}
        sandbox={SANDBOX_ATTRIBUTES}
        onLoad={handleIframeLoad}
        onError={handleIframeError}
        style={{
          width: '100%',
          height: '100%',
          border: 'none',
          display: error ? 'none' : 'block',
        }}
        title="Code Preview"
      />
    </div>
  );
};

export default CodeExecutor;

