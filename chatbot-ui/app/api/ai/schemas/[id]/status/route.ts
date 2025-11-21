import {createStatusHandlers} from "../../../_proxy";

const handlers = createStatusHandlers("/ai/schemas", "AI Schema");

export const PATCH = handlers.PATCH;
