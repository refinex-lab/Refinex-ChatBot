import {createStatusHandlers} from "../../../_proxy";

const handlers = createStatusHandlers("/ai/tools", "AI 工具");

export const PATCH = handlers.PATCH;
