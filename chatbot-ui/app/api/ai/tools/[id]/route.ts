import {createEntityHandlers} from "../../_proxy";

const handlers = createEntityHandlers("/ai/tools", "AI 工具");

export const GET = handlers.GET;
export const PUT = handlers.PUT;
export const DELETE = handlers.DELETE;
