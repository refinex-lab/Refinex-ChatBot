import {createCollectionHandlers} from "../_proxy";

const handlers = createCollectionHandlers("/ai/schemas", "AI Schema");

export const GET = handlers.GET;
export const POST = handlers.POST;
