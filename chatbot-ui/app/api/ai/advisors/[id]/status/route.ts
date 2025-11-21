import {createStatusHandlers} from "../../../_proxy";

const handlers = createStatusHandlers("/ai/advisors", "AI Advisor");

export const PATCH = handlers.PATCH;
