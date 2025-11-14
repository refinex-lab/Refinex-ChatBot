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

/**
 * DataSign 请求头名称（用于后端日志跟踪/验签）
 * 默认：DataSign，可通过环境变量覆盖
 */
const PRIVATE_DATA_SIGN_HEADER = process.env.DATA_SIGN_HEADER;
const PUBLIC_DATA_SIGN_HEADER = process.env.NEXT_PUBLIC_DATA_SIGN_HEADER;
export const DATA_SIGN_HEADER = (PUBLIC_DATA_SIGN_HEADER ?? PRIVATE_DATA_SIGN_HEADER ?? "DataSign").trim();

/**
 * 后端认证 Cookie 名称（Sa-Token 名称）
 * 默认：Authorization（与后端 sa-token.token-name 对齐），可通过环境覆盖
 */
const PRIVATE_AUTH_COOKIE_NAME = process.env.AUTH_COOKIE_NAME;
const PUBLIC_AUTH_COOKIE_NAME = process.env.NEXT_PUBLIC_AUTH_COOKIE_NAME;
export const AUTH_COOKIE_NAME = (PUBLIC_AUTH_COOKIE_NAME ?? PRIVATE_AUTH_COOKIE_NAME ?? "Authorization").trim();
