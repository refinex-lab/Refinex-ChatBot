import {createEntityHandlers} from "../../_proxy";

const handlers = createEntityHandlers("/ai/schemas", "AI Schema");

export const GET = handlers.GET;
export const PUT = handlers.PUT;
export const DELETE = handlers.DELETE;
