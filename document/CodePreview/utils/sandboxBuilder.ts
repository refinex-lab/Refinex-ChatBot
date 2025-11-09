/**
 * SandboxBuilder - 沙箱代码构建器
 *
 * 功能：
 * - 将不同类型的代码转换为可在 iframe 中执行的 HTML
 * - 注入必要的运行时依赖（React/Vue CDN）
 * - 添加错误捕获和日志记录
 *
 * @author Refinex Team
 */

import type {CodeExecutionConfig, CodeExecutionResult} from '../types';

/**
 * 默认 CDN 地址
 */
const DEFAULT_CDN = {
  react: 'https://unpkg.com/react@18/umd/react.production.min.js',
  reactDOM: 'https://unpkg.com/react-dom@18/umd/react-dom.production.min.js',
  babel: 'https://unpkg.com/@babel/standalone/babel.min.js',
  vue: 'https://unpkg.com/vue@3/dist/vue.global.prod.js',
};

/**
 * iframe sandbox 属性
 */
export const SANDBOX_ATTRIBUTES = [
  'allow-scripts',
  'allow-same-origin',
  'allow-forms',
  'allow-modals',
  'allow-popups',
].join(' ');

/**
 * 构建可执行的 HTML
 */
export function buildExecutableHTML(config: CodeExecutionConfig): CodeExecutionResult {
  const { code, type, enableConsole = false, cdnUrls = {} } = config;

  try {
    let html: string;

    switch (type) {
      case 'html':
        html = buildHTMLSandbox(code, enableConsole);
        break;
      case 'react':
        html = buildReactSandbox(code, enableConsole, cdnUrls);
        break;
      case 'vue':
        html = buildVueSandbox(code, enableConsole, cdnUrls);
        break;
      case 'svg':
        html = buildSVGSandbox(code, enableConsole);
        break;
      default:
        return {
          success: false,
          error: {
            message: `不支持的代码类型: ${type}`,
          },
        };
    }

    return {
      success: true,
      html,
    };
  } catch (error) {
    return {
      success: false,
      error: {
        message: error instanceof Error ? error.message : '构建失败',
        stack: error instanceof Error ? error.stack : undefined,
      },
    };
  }
}

/**
 * 构建 HTML 沙箱
 */
function buildHTMLSandbox(code: string, enableConsole: boolean): string {
  const consoleScript = enableConsole ? getConsoleInterceptScript() : '';
  const errorHandlerScript = getErrorHandlerScript();

  // 如果代码已经是完整的 HTML，直接注入脚本
  if (/<!DOCTYPE\s+html>/i.test(code) || /<html[^>]*>/i.test(code)) {
    return injectScriptsToHTML(code, consoleScript + errorHandlerScript);
  }

  // 否则包装成完整的 HTML
  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Preview</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
      padding: 16px;
    }
  </style>
</head>
<body>
  ${code}
  <script>
    ${errorHandlerScript}
    ${consoleScript}
  </script>
</body>
</html>
  `.trim();
}

/**
 * 构建 React 沙箱
 */
function buildReactSandbox(
  code: string,
  enableConsole: boolean,
  cdnUrls: Partial<typeof DEFAULT_CDN>,
): string {
  const reactUrl = cdnUrls.react || DEFAULT_CDN.react;
  const reactDOMUrl = cdnUrls.reactDOM || DEFAULT_CDN.reactDOM;
  const babelUrl = cdnUrls.babel || DEFAULT_CDN.babel;

  const consoleScript = enableConsole ? getConsoleInterceptScript() : '';
  const errorHandlerScript = getErrorHandlerScript();

  // 提取导入语句（在浏览器环境中需要移除）
  const cleanedCode = code.replace(/import\s+.*\s+from\s+['"]react['"]\s*;?/gi, '');

  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>React Preview</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    }
    #root {
      width: 100%;
      min-height: 100vh;
    }
  </style>
  <script crossorigin src="${reactUrl}"></script>
  <script crossorigin src="${reactDOMUrl}"></script>
  <script src="${babelUrl}"></script>
</head>
<body>
  <div id="root"></div>
  <script>
    ${errorHandlerScript}
    ${consoleScript}
  </script>
  <script type="text/babel">
    const { useState, useEffect, useContext, useReducer, useCallback, useMemo, useRef, createContext } = React;

    ${cleanedCode}

    // 自动查找并渲染组件
    (function() {
      try {
        const rootElement = document.getElementById('root');
        if (!rootElement) {
          throw new Error('Root element not found');
        }

        const root = ReactDOM.createRoot(rootElement);

        // 尝试查找默认导出或命名导出的组件
        let ComponentToRender = null;

        // 1. 查找默认导出
        if (typeof App !== 'undefined') {
          ComponentToRender = App;
        }

        // 2. 查找其他可能的组件名
        const possibleNames = ['Main', 'Component', 'Index', 'Demo', 'Example'];
        for (const name of possibleNames) {
          if (typeof window[name] !== 'undefined' && typeof window[name] === 'function') {
            ComponentToRender = window[name];
            break;
          }
        }

        // 3. 查找任何看起来像 React 组件的函数
        if (!ComponentToRender) {
          const componentPattern = /^[A-Z]/;
          for (const key in window) {
            if (componentPattern.test(key) && typeof window[key] === 'function') {
              const fnStr = window[key].toString();
              if (fnStr.includes('React.createElement') || fnStr.includes('return')) {
                ComponentToRender = window[key];
                break;
              }
            }
          }
        }

        if (ComponentToRender) {
          root.render(React.createElement(ComponentToRender));
        } else {
          throw new Error('No React component found. Please export a component named "App" or use export default.');
        }
      } catch (error) {
        console.error('Render error:', error);
        document.getElementById('root').innerHTML = \`
          <div style="padding: 20px; color: #d32f2f; background: #ffebee; border-left: 4px solid #d32f2f;">
            <h3 style="margin: 0 0 10px 0;">渲染错误</h3>
            <p style="margin: 0; font-family: monospace;">\${error.message}</p>
          </div>
        \`;
      }
    })();
  </script>
</body>
</html>
  `.trim();
}

/**
 * 构建 Vue 沙箱
 */
function buildVueSandbox(
  code: string,
  enableConsole: boolean,
  cdnUrls: Partial<typeof DEFAULT_CDN>,
): string {
  const vueUrl = cdnUrls.vue || DEFAULT_CDN.vue;

  const consoleScript = enableConsole ? getConsoleInterceptScript() : '';
  const errorHandlerScript = getErrorHandlerScript();

  // 提取 template、script、style
  const template = extractVueSection(code, 'template');
  const script = extractVueSection(code, 'script');
  const style = extractVueSection(code, 'style');

  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Vue Preview</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    }
    #app {
      width: 100%;
      min-height: 100vh;
    }
    ${style}
  </style>
  <script src="${vueUrl}"></script>
</head>
<body>
  <div id="app"></div>
  <script>
    ${errorHandlerScript}
    ${consoleScript}

    (function() {
      try {
        const { createApp, ref, reactive, computed, watch, onMounted, onUnmounted } = Vue;

        // 解析组件选项
        let componentOptions = {};
        ${script}

        // 如果有 export default，使用它
        if (typeof exports !== 'undefined' && exports.default) {
          componentOptions = exports.default;
        }

        // 添加 template
        componentOptions.template = \`${template.replace(/`/g, '\\`')}\`;

        // 创建应用
        const app = createApp(componentOptions);

        // 全局错误处理
        app.config.errorHandler = (err, instance, info) => {
          console.error('Vue error:', err, info);
        };

        app.mount('#app');
      } catch (error) {
        console.error('Mount error:', error);
        document.getElementById('app').innerHTML = \`
          <div style="padding: 20px; color: #d32f2f; background: #ffebee; border-left: 4px solid #d32f2f;">
            <h3 style="margin: 0 0 10px 0;">挂载错误</h3>
            <p style="margin: 0; font-family: monospace;">\${error.message}</p>
          </div>
        \`;
      }
    })();
  </script>
</body>
</html>
  `.trim();
}

/**
 * 构建 SVG 沙箱
 */
function buildSVGSandbox(code: string, enableConsole: boolean): string {
  const consoleScript = enableConsole ? getConsoleInterceptScript() : '';
  const errorHandlerScript = getErrorHandlerScript();

  return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SVG Preview</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: #f5f5f5;
    }
    svg {
      max-width: 100%;
      max-height: 100vh;
    }
  </style>
</head>
<body>
  ${code}
  <script>
    ${errorHandlerScript}
    ${consoleScript}
  </script>
</body>
</html>
  `.trim();
}

/**
 * 获取控制台拦截脚本
 */
function getConsoleInterceptScript(): string {
  return `
    // 拦截 console 输出并发送到父窗口
    (function() {
      const originalConsole = {
        log: console.log,
        error: console.error,
        warn: console.warn,
        info: console.info,
      };

      ['log', 'error', 'warn', 'info'].forEach(method => {
        console[method] = function(...args) {
          originalConsole[method].apply(console, args);
          window.parent.postMessage({
            type: 'console',
            method: method,
            args: args.map(arg => {
              try {
                return typeof arg === 'object' ? JSON.stringify(arg) : String(arg);
              } catch (e) {
                return String(arg);
              }
            }),
          }, '*');
        };
      });
    })();
  `;
}

/**
 * 获取错误处理脚本
 */
function getErrorHandlerScript(): string {
  return `
    // 全局错误捕获
    window.addEventListener('error', function(event) {
      window.parent.postMessage({
        type: 'error',
        message: event.message,
        filename: event.filename,
        lineno: event.lineno,
        colno: event.colno,
        error: event.error ? event.error.stack : null,
      }, '*');
    });

    // Promise 错误捕获
    window.addEventListener('unhandledrejection', function(event) {
      window.parent.postMessage({
        type: 'error',
        message: 'Unhandled Promise Rejection: ' + event.reason,
      }, '*');
    });
  `;
}

/**
 * 向 HTML 中注入脚本
 */
function injectScriptsToHTML(html: string, scripts: string): string {
  // 尝试在 </body> 前注入
  if (/<\/body>/i.test(html)) {
    return html.replace(/<\/body>/i, `<script>${scripts}</script></body>`);
  }

  // 尝试在 </html> 前注入
  if (/<\/html>/i.test(html)) {
    return html.replace(/<\/html>/i, `<script>${scripts}</script></html>`);
  }

  // 否则追加到末尾
  return html + `<script>${scripts}</script>`;
}

/**
 * 提取 Vue SFC 的某个部分
 */
function extractVueSection(code: string, section: 'template' | 'script' | 'style'): string {
  const regex = new RegExp(`<${section}[^>]*>([\\s\\S]*?)<\\/${section}>`, 'i');
  const match = code.match(regex);
  return match ? match[1].trim() : '';
}

