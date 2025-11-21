import {createCollectionHandlers} from "../_proxy";

const handlers = createCollectionHandlers("/ai/agents", "AI Agent");

export const GET = handlers.GET;
export const POST = handlers.POST;
