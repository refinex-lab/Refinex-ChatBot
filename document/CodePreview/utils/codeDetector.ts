/**
 * CodeDetector - 代码类型检测工具
 *
 * 功能：
 * - 检测代码是否可执行
 * - 识别代码类型（HTML/React/Vue/SVG）
 * - 计算检测置信度
 *
 * @author Refinex Team
 */

import type {CodeDetectionResult, CodeType} from '../types';

/**
 * 检测代码是否可执行及其类型
 */
export function detectExecutableCode(
  code: string,
  language: string,
): CodeDetectionResult {
  if (!code || !code.trim()) {
    return { executable: false, type: null, confidence: 0 };
  }

  // 按优先级检测各种类型
  const detectors = [
    detectHTML,
    detectReact,
    detectVue,
    detectSVG,
  ];

  for (const detector of detectors) {
    const result = detector(code, language);
    if (result.executable && result.confidence > 0.6) {
      return result;
    }
  }

  return { executable: false, type: null, confidence: 0 };
}

/**
 * 检测 HTML 代码
 */
function detectHTML(code: string, language: string): CodeDetectionResult {
  const features: string[] = [];
  let confidence = 0;

  // 语言匹配
  if (language.toLowerCase() === 'html') {
    confidence += 0.3;
    features.push('language:html');
  }

  // DOCTYPE 声明
  if (/<!DOCTYPE\s+html>/i.test(code)) {
    confidence += 0.3;
    features.push('doctype');
  }

  // HTML 标签
  if (/<html[^>]*>/i.test(code)) {
    confidence += 0.2;
    features.push('html-tag');
  }

  // 完整结构（head + body）
  const hasHead = /<head[^>]*>[\s\S]*<\/head>/i.test(code);
  const hasBody = /<body[^>]*>[\s\S]*<\/body>/i.test(code);

  if (hasHead && hasBody) {
    confidence += 0.4;
    features.push('complete-structure');
  } else if (hasBody) {
    confidence += 0.2;
    features.push('body-tag');
  }

  // 包含样式或脚本
  if (/<style[^>]*>[\s\S]*<\/style>/i.test(code)) {
    confidence += 0.1;
    features.push('style-tag');
  }
  if (/<script[^>]*>[\s\S]*<\/script>/i.test(code)) {
    confidence += 0.1;
    features.push('script-tag');
  }

  // 最低要求：必须有 DOCTYPE 或完整的 html 标签，或者 head+body
  const executable = confidence >= 0.5 && (
    features.includes('doctype') ||
    features.includes('html-tag') ||
    features.includes('complete-structure')
  );

  return {
    executable,
    type: executable ? 'html' : null,
    confidence: Math.min(confidence, 1),
    features,
  };
}

/**
 * 检测 React 组件
 */
function detectReact(code: string, language: string): CodeDetectionResult {
  const features: string[] = [];
  let confidence = 0;

  // 语言匹配
  const jsLangs = ['javascript', 'jsx', 'typescript', 'tsx', 'js', 'ts'];
  if (jsLangs.includes(language.toLowerCase())) {
    confidence += 0.2;
    features.push(`language:${language}`);
  }

  // React 导入
  if (/import\s+.*\s+from\s+['"]react['"]/i.test(code)) {
    confidence += 0.3;
    features.push('react-import');
  }

  // React Hooks
  const hookPattern = /\b(useState|useEffect|useContext|useReducer|useCallback|useMemo|useRef)\s*\(/g;
  const hookMatches = code.match(hookPattern);
  if (hookMatches && hookMatches.length > 0) {
    confidence += 0.2;
    features.push('react-hooks');
  }

  // JSX 语法
  const jsxPattern = /<[A-Z][a-zA-Z0-9]*[^>]*>|<[a-z][a-zA-Z0-9-]*[^>]*>/g;
  const jsxMatches = code.match(jsxPattern);
  if (jsxMatches && jsxMatches.length >= 2) {
    confidence += 0.3;
    features.push('jsx-syntax');
  }

  // 函数组件定义
  const functionComponentPattern = /function\s+([A-Z][a-zA-Z0-9]*)\s*\([^)]*\)\s*{[\s\S]*return\s*\(/;
  const arrowComponentPattern = /const\s+([A-Z][a-zA-Z0-9]*)\s*=\s*\([^)]*\)\s*=>\s*{[\s\S]*return\s*\(/;
  const arrowComponentShortPattern = /const\s+([A-Z][a-zA-Z0-9]*)\s*=\s*\([^)]*\)\s*=>\s*\(/;

  if (functionComponentPattern.test(code) ||
      arrowComponentPattern.test(code) ||
      arrowComponentShortPattern.test(code)) {
    confidence += 0.3;
    features.push('component-definition');
  }

  // Export 语句
  if (/export\s+(default|{)/.test(code)) {
    confidence += 0.1;
    features.push('export-statement');
  }

  // 最低要求：必须有 React 导入或明显的 JSX 语法
  const executable = confidence >= 0.6 && (
    features.includes('react-import') ||
    (features.includes('jsx-syntax') && features.includes('component-definition'))
  );

  return {
    executable,
    type: executable ? 'react' : null,
    confidence: Math.min(confidence, 1),
    features,
  };
}

/**
 * 检测 Vue 组件
 */
function detectVue(code: string, language: string): CodeDetectionResult {
  const features: string[] = [];
  let confidence = 0;

  // 语言匹配
  if (language.toLowerCase() === 'vue') {
    confidence += 0.3;
    features.push('language:vue');
  }

  // Template 标签
  const hasTemplate = /<template[^>]*>[\s\S]*<\/template>/i.test(code);
  if (hasTemplate) {
    confidence += 0.4;
    features.push('template-tag');
  }

  // Script 标签
  const hasScript = /<script[^>]*>[\s\S]*<\/script>/i.test(code);
  if (hasScript) {
    confidence += 0.3;
    features.push('script-tag');
  }

  // Style 标签
  const hasStyle = /<style[^>]*>[\s\S]*<\/style>/i.test(code);
  if (hasStyle) {
    confidence += 0.1;
    features.push('style-tag');
  }

  // Vue 3 Composition API
  if (/import\s+{[^}]*(ref|reactive|computed|watch)[^}]*}\s+from\s+['"]vue['"]/i.test(code)) {
    confidence += 0.2;
    features.push('composition-api');
  }

  // Vue 2 Options API
  if (/export\s+default\s+{[\s\S]*(data|methods|computed|mounted)\s*[:(]/i.test(code)) {
    confidence += 0.2;
    features.push('options-api');
  }

  // 最低要求：必须有 template 和 script
  const executable = confidence >= 0.6 && hasTemplate && hasScript;

  return {
    executable,
    type: executable ? 'vue' : null,
    confidence: Math.min(confidence, 1),
    features,
  };
}

/**
 * 检测 SVG 代码
 */
function detectSVG(code: string, language: string): CodeDetectionResult {
  const features: string[] = [];
  let confidence = 0;

  // 语言匹配
  if (['svg', 'xml'].includes(language.toLowerCase())) {
    confidence += 0.3;
    features.push(`language:${language}`);
  }

  // SVG 标签
  if (/<svg[^>]*>[\s\S]*<\/svg>/i.test(code)) {
    confidence += 0.5;
    features.push('svg-tag');
  }

  // SVG 命名空间
  if (/xmlns="http:\/\/www\.w3\.org\/2000\/svg"/i.test(code)) {
    confidence += 0.2;
    features.push('svg-namespace');
  }

  // SVG 元素
  const svgElements = ['path', 'circle', 'rect', 'line', 'polygon', 'polyline', 'ellipse', 'g'];
  const foundElements = svgElements.filter(el =>
    new RegExp(`<${el}[^>]*>`, 'i').test(code)
  );

  if (foundElements.length > 0) {
    confidence += 0.1 * Math.min(foundElements.length, 3);
    features.push(`svg-elements:${foundElements.length}`);
  }

  // 最低要求：必须有 SVG 标签
  const executable = confidence >= 0.6 && features.includes('svg-tag');

  return {
    executable,
    type: executable ? 'svg' : null,
    confidence: Math.min(confidence, 1),
    features,
  };
}

/**
 * 获取代码类型的友好名称
 */
export function getCodeTypeName(type: CodeType): string {
  const names: Record<NonNullable<CodeType>, string> = {
    html: 'HTML',
    react: 'React',
    vue: 'Vue',
    svg: 'SVG',
  };

  return type ? names[type] : 'Unknown';
}

/**
 * 检查代码是否包含潜在的危险操作
 */
export function checkCodeSafety(code: string): {
  safe: boolean;
  warnings: string[];
} {
  const warnings: string[] = [];

  // 检查危险的 API 调用
  const dangerousAPIs = [
    { pattern: /eval\s*\(/g, message: '包含 eval() 调用' },
    { pattern: /Function\s*\(/g, message: '包含 Function 构造函数' },
    { pattern: /document\.write\s*\(/g, message: '包含 document.write()' },
    { pattern: /localStorage/g, message: '访问 localStorage' },
    { pattern: /sessionStorage/g, message: '访问 sessionStorage' },
    { pattern: /indexedDB/g, message: '访问 indexedDB' },
    { pattern: /XMLHttpRequest/g, message: '包含 XMLHttpRequest' },
    { pattern: /fetch\s*\(/g, message: '包含 fetch 请求' },
    { pattern: /WebSocket/g, message: '包含 WebSocket' },
  ];

  for (const { pattern, message } of dangerousAPIs) {
    if (pattern.test(code)) {
      warnings.push(message);
    }
  }

  return {
    safe: warnings.length === 0,
    warnings,
  };
}

