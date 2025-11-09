/**
 * 设备预设配置
 * 参考 Chrome DevTools 的设备列表
 *
 * @author Refinex Team
 */

import type {DeviceInfo} from './types';

/**
 * 预设设备列表
 */
export const DEVICE_PRESETS: DeviceInfo[] = [
  // ==================== 桌面设备 ====================
  {
    id: 'desktop',
    name: '桌面设备（响应式）',
    type: 'desktop',
    width: 1920,
    height: 1080,
    pixelRatio: 1,
  },

  // ==================== iPhone ====================
  {
    id: 'iphone-se',
    name: 'iPhone SE',
    type: 'mobile',
    width: 375,
    height: 667,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (iPhone; CPU iPhone OS 13_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/604.1',
  },
  {
    id: 'iphone-xr',
    name: 'iPhone XR',
    type: 'mobile',
    width: 414,
    height: 896,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1',
  },
  {
    id: 'iphone-12-pro',
    name: 'iPhone 12 Pro',
    type: 'mobile',
    width: 390,
    height: 844,
    pixelRatio: 3,
    userAgent:
      'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1',
  },
  {
    id: 'iphone-14-pro-max',
    name: 'iPhone 14 Pro Max',
    type: 'mobile',
    width: 430,
    height: 932,
    pixelRatio: 3,
    userAgent:
      'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1',
  },

  // ==================== Android ====================
  {
    id: 'pixel-7',
    name: 'Pixel 7',
    type: 'mobile',
    width: 412,
    height: 915,
    pixelRatio: 2.625,
    userAgent:
      'Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36',
  },
  {
    id: 'samsung-galaxy-s8',
    name: 'Samsung Galaxy S8+',
    type: 'mobile',
    width: 360,
    height: 740,
    pixelRatio: 4,
    userAgent:
      'Mozilla/5.0 (Linux; Android 9; SM-G955F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36',
  },
  {
    id: 'samsung-galaxy-s20-ultra',
    name: 'Samsung Galaxy S20 Ultra',
    type: 'mobile',
    width: 412,
    height: 915,
    pixelRatio: 3.5,
    userAgent:
      'Mozilla/5.0 (Linux; Android 11; SM-G988B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36',
  },
  {
    id: 'samsung-galaxy-a51-71',
    name: 'Samsung Galaxy A51/71',
    type: 'mobile',
    width: 412,
    height: 914,
    pixelRatio: 2.625,
    userAgent:
      'Mozilla/5.0 (Linux; Android 10; SM-A515F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36',
  },

  // ==================== iPad ====================
  {
    id: 'ipad-mini',
    name: 'iPad Mini',
    type: 'tablet',
    width: 768,
    height: 1024,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1',
  },
  {
    id: 'ipad-air',
    name: 'iPad Air',
    type: 'tablet',
    width: 820,
    height: 1180,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1',
  },
  {
    id: 'ipad-pro',
    name: 'iPad Pro',
    type: 'tablet',
    width: 1024,
    height: 1366,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1',
  },

  // ==================== Android 平板 ====================
  {
    id: 'surface-pro-7',
    name: 'Surface Pro 7',
    type: 'tablet',
    width: 912,
    height: 1368,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
  },
  {
    id: 'surface-duo',
    name: 'Surface Duo',
    type: 'tablet',
    width: 540,
    height: 720,
    pixelRatio: 2.5,
    userAgent:
      'Mozilla/5.0 (Linux; Android 11; Surface Duo) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36',
  },
  {
    id: 'galaxy-fold',
    name: 'Samsung Galaxy Fold',
    type: 'tablet',
    width: 280,
    height: 653,
    pixelRatio: 3,
    userAgent:
      'Mozilla/5.0 (Linux; Android 10; SM-F900F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36',
  },

  // ==================== 其他设备 ====================
  {
    id: 'nest-hub',
    name: 'Nest Hub',
    type: 'tablet',
    width: 1024,
    height: 600,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Safari/537.36',
  },
  {
    id: 'nest-hub-max',
    name: 'Nest Hub Max',
    type: 'tablet',
    width: 1280,
    height: 800,
    pixelRatio: 2,
    userAgent:
      'Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Safari/537.36',
  },
];

/**
 * 根据设备 ID 获取设备信息
 */
export function getDeviceById(id: string): DeviceInfo | undefined {
  return DEVICE_PRESETS.find((device) => device.id === id);
}

/**
 * 根据设备类型获取设备列表
 */
export function getDevicesByType(type: 'desktop' | 'tablet' | 'mobile'): DeviceInfo[] {
  return DEVICE_PRESETS.filter((device) => device.type === type);
}

/**
 * 获取所有移动设备
 */
export function getMobileDevices(): DeviceInfo[] {
  return getDevicesByType('mobile');
}

/**
 * 获取所有平板设备
 */
export function getTabletDevices(): DeviceInfo[] {
  return getDevicesByType('tablet');
}

/**
 * 缩放比例选项
 */
export const ZOOM_LEVELS = [25, 50, 75, 100, 125, 150] as const;

/**
 * 默认缩放比例
 */
export const DEFAULT_ZOOM_LEVEL = 100;

