/**
 * AI 聊天首页问候语
 */
"use client";

import {motion} from "framer-motion";
import {useEffect, useState} from "react";

export const Greeting = () => {
  const [nickname, setNickname] = useState<string>("");

  useEffect(() => {
    let aborted = false;
    (async () => {
      try {
        const resp = await fetch("/api/auth/me", { method: "GET" });
        const data = await resp.json().catch(() => ({}));
        // 期望后端返回：{ code, data: { nickname, username, email } }
        if (data && data.code === 200 && data.data && !aborted) {
          const nn =
            data.data.nickname ||
            data.data.username ||
            data.data.email ||
            "";
          setNickname(nn as string);
        }
      } catch {
        // ignore
      }
    })();
    return () => {
      aborted = true;
    };
  }, []);

  return (
    <div
      className="mx-auto mt-4 flex size-full max-w-3xl flex-col justify-center px-4 md:mt-16 md:px-8"
      key="overview"
    >
      <motion.div
        animate={{ opacity: 1, y: 0 }}
        className="font-semibold text-xl md:text-2xl"
        exit={{ opacity: 0, y: 10 }}
        initial={{ opacity: 0, y: 10 }}
        transition={{ delay: 0.5 }}
      >
        你好！{nickname ? nickname : null}
      </motion.div>
      <motion.div
        animate={{ opacity: 1, y: 0 }}
        className="text-xl text-zinc-500 md:text-2xl"
        exit={{ opacity: 0, y: 10 }}
        initial={{ opacity: 0, y: 10 }}
        transition={{ delay: 0.6 }}
      >
        今天我能帮您做些什么呢？
      </motion.div>
    </div>
  );
};
