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

"use client";

import {useEffect, useRef, useState} from 'react';
import {buildExecutableHTML, SANDBOX_ATTRIBUTES} from './utils/sandbox-builder';
import type {CodeExecutorProps} from './types';
import {cn} from '@/lib/utils';

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
    <div className={cn('code-executor relative w-full h-full', className)}>
      {/* 加载状态 */}
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-background/90 z-10">
          <div className="flex flex-col items-center gap-2">
            <div className="size-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
            <span className="text-sm text-muted-foreground">正在加载...</span>
          </div>
        </div>
      )}

      {/* 错误提示 */}
      {error && !loading && (
        <div className="p-4">
          <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4">
            <div className="flex items-start gap-2">
              <div className="size-5 rounded-full bg-destructive/20 flex items-center justify-center flex-shrink-0 mt-0.5">
                <span className="text-xs text-destructive font-bold">!</span>
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-sm font-semibold text-destructive mb-1">执行错误</h4>
                <p className="text-sm text-destructive/90 break-words">{error}</p>
              </div>
              <button
                type="button"
                onClick={() => setError(null)}
                className="text-destructive/70 hover:text-destructive transition-colors flex-shrink-0"
                aria-label="关闭错误提示"
              >
                <svg
                  className="size-4"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 控制台输出（开发模式） */}
      {process.env.NODE_ENV === 'development' && consoleOutput.length > 0 && (
        <div className="absolute bottom-0 left-0 right-0 max-h-[200px] overflow-auto bg-[#1e1e1e] text-[#d4d4d4] p-2 text-xs font-mono z-[5] border-t border-[#333]">
          {consoleOutput.map((output, index) => (
            <div key={index} className="py-0.5">
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
        className={cn(
          'w-full h-full border-0',
          error ? 'hidden' : 'block'
        )}
        title="Code Preview"
      />
    </div>
  );
};

export default CodeExecutor;

