/**
 * 环境变量工具
 */
// 请求根地址
const PRIVATE_BASE_URL = process.env.BASE_URL;
const PUBLIC_BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

// 平台服务前缀
const PRIVATE_PLATFORM_PREFIX = process.env.BASE_URL_PREFIX_PLATFORM;
const PUBLIC_PLATFORM_PREFIX = process.env.NEXT_PUBLIC_BASE_URL_PREFIX_PLATFORM;

// 知识库服务前缀
const PRIVATE_KB_PREFIX = process.env.BASE_URL_PREFIX_KB;
const PUBLIC_KB_PREFIX = process.env.NEXT_PUBLIC_BASE_URL_PREFIX_KB;

// AI服务前缀
const PRIVATE_AI_PREFIX = process.env.BASE_URL_PREFIX_AI;
const PUBLIC_AI_PREFIX = process.env.NEXT_PUBLIC_BASE_URL_PREFIX_AI;

// 最终请求根地址和前缀
const baseUrl = (PUBLIC_BASE_URL ?? PRIVATE_BASE_URL ?? "").replace(/\/+$/, "");
const platformPrefix = (PUBLIC_PLATFORM_PREFIX ?? PRIVATE_PLATFORM_PREFIX ?? "").replace(/^\//, "");
const kbPrefix = (PUBLIC_KB_PREFIX ?? PRIVATE_KB_PREFIX ?? "").replace(/^\//, "");
const aiPrefix = (PUBLIC_AI_PREFIX ?? PRIVATE_AI_PREFIX ?? "").replace(/^\//, "");

// 校验必须配置的环境变量
if (!baseUrl) {
  throw new Error("BASE_URL / NEXT_PUBLIC_BASE_URL 未配置");
}
if (!platformPrefix) {
  throw new Error("BASE_URL_PREFIX_PLATFORM / NEXT_PUBLIC_BASE_URL_PREFIX_PLATFORM 未配置");
}
if (!kbPrefix) {
  throw new Error("BASE_URL_PREFIX_KB / NEXT_PUBLIC_BASE_URL_PREFIX_KB 未配置");
}
if (!aiPrefix) {
  throw new Error("BASE_URL_PREFIX_AI / NEXT_PUBLIC_BASE_URL_PREFIX_AI 未配置");
}

/**
 * 平台服务基础地址 (带网关前缀)
 */
export const PLATFORM_SERVICE_BASE_URL = `${baseUrl}/${platformPrefix}`;

/**
 * 平台认证基础地址
 */
export const PLATFORM_AUTH_BASE_URL = `${PLATFORM_SERVICE_BASE_URL}/auth`;

/**
 * 平台验证码地址
 */
export const PLATFORM_CAPTCHA_URL = `${PLATFORM_SERVICE_BASE_URL}/captcha`;
