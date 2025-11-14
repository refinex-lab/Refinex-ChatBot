import {generateDummyPassword} from "./db/utils";

export const isProductionEnvironment = process.env.NODE_ENV === "production";
export const isDevelopmentEnvironment = process.env.NODE_ENV === "development";
export const isTestEnvironment = Boolean(
  process.env.PLAYWRIGHT_TEST_BASE_URL ||
    process.env.PLAYWRIGHT ||
    process.env.CI_PLAYWRIGHT
);

export const DUMMY_PASSWORD = generateDummyPassword();

/**
 * 是否强制未登录跳到登录页
 * - 默认 false（允许游客模式）
 * - 可通过 NEXT_PUBLIC_REQUIRE_LOGIN / REQUIRE_LOGIN 开启强制登录
 */
export const requireLogin =
  (process.env.NEXT_PUBLIC_REQUIRE_LOGIN ?? process.env.REQUIRE_LOGIN ?? "false")
    .toLowerCase() === "true";
