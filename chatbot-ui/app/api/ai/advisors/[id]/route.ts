import {createEntityHandlers} from "../../_proxy";

const handlers = createEntityHandlers("/ai/advisors", "AI Advisor");

export const GET = handlers.GET;
export const PUT = handlers.PUT;
export const DELETE = handlers.DELETE;
