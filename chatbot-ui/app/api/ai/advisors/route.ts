import {createCollectionHandlers} from "../_proxy";

const handlers = createCollectionHandlers("/ai/advisors", "AI Advisor");

export const GET = handlers.GET;
export const POST = handlers.POST;
