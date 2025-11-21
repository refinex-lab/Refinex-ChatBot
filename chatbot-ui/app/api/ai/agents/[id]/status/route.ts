import {createStatusHandlers} from "../../../_proxy";

const handlers = createStatusHandlers("/ai/agents", "AI Agent");

export const PATCH = handlers.PATCH;
