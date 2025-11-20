/**
 * 常规设置：外观/重点色
 */
"use client";

import {useEffect, useState} from "react";
import {useTheme} from "next-themes";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";

type AccentColor = "default" | "blue" | "green" | "yellow" | "pink" | "orange";

const ACCENT_MAP: Record<AccentColor, string> = {
  default: "#000000", // tailwind blue-500 作为默认
  blue: "#3b82f6",
  green: "#22c55e",
  yellow: "#eab308",
  pink: "#ec4899",
  orange: "#f97316",
};

function setAccent(color: AccentColor) {
  const hex = ACCENT_MAP[color] || ACCENT_MAP.default;
  // 运行时切换品牌色，影响 primary/ring/sidebar 等
  const root = document.documentElement;
  root.style.setProperty("--brand-color", hex);
  root.style.setProperty("--ring", hex);
  root.style.setProperty("--sidebar-ring", hex);
  localStorage.setItem("rx-accent", color);
}

function getAccent(): AccentColor {
  const v = (typeof window !== "undefined" && localStorage.getItem("rx-accent")) as AccentColor | null;
  return v || "default";
}

const Row = ({ label, children }: { label: string; children: React.ReactNode }) => (
  <div className="grid grid-cols-[160px_1fr] items-center gap-4 border-b px-6 py-4 last:border-b-0">
    <div className="text-sm text-muted-foreground">{label}</div>
    <div className="flex w-full items-center justify-end gap-3">{children}</div>
  </div>
);

export function GeneralSettings() {
  const { resolvedTheme, setTheme } = useTheme();
  const [accent, setAccentState] = useState<AccentColor>("default");

  useEffect(() => {
    const a = getAccent();
    setAccentState(a);
    setAccent(a);
  }, []);

  return (
    <div>
      <div className="border-b px-6 py-3">
        <h2 className="text-lg font-semibold">常规</h2>
      </div>

      <Row label="外观">
        <Select
          value={(resolvedTheme as string) || "system"}
          onValueChange={(v) => setTheme(v as "system" | "light" | "dark")}
        >
          <SelectTrigger className="w-44 justify-between">
            <SelectValue placeholder="系统" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="system">系统</SelectItem>
            <SelectItem value="light">浅色</SelectItem>
            <SelectItem value="dark">深色</SelectItem>
          </SelectContent>
        </Select>
      </Row>

      <Row label="重点色">
        <Select
          value={accent}
          onValueChange={(v) => {
            const val = v as AccentColor;
            setAccentState(val);
            setAccent(val);
          }}
        >
          <SelectTrigger className="w-44 justify-between">
            <SelectValue placeholder="默认" />
          </SelectTrigger>
          <SelectContent>
            {(["default","blue","green","yellow","pink","orange"] as AccentColor[]).map((c) => (
              <SelectItem key={c} value={c}>
                <span className="inline-flex items-center gap-2">
                  <span
                    className="inline-block h-3 w-3 rounded-full border"
                    style={{ backgroundColor: ACCENT_MAP[c] }}
                  />
                  {c === "default" ? "默认" :
                   c === "blue" ? "蓝色" :
                   c === "green" ? "绿色" :
                   c === "yellow" ? "黄色" :
                   c === "pink" ? "粉色" :
                   "橙色"}
                </span>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </Row>
    </div>
  );
}
