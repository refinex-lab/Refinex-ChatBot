import {PLATFORM_CAPTCHA_URL} from "@/lib/env";
import type {ApiResponse} from "@/lib/types/api";

export type CaptchaPayload = {
  uuid: string;
  image: string;
  expireSeconds: number;
};

/**
 * 请求验证码
 */
export async function requestCaptcha(): Promise<CaptchaPayload> {
  const response = await fetch(PLATFORM_CAPTCHA_URL, {
    method: "GET",
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("获取验证码失败");
  }

  const result = (await response.json()) as ApiResponse<CaptchaPayload | null>;
  if (result.code !== 200 || !result.data) {
    throw new Error(result.msg || "验证码获取失败");
  }

  return result.data;
}
