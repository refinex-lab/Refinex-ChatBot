import {createCollectionHandlers} from "../_proxy";

const handlers = createCollectionHandlers("/ai/tools", "AI 工具");

export const GET = handlers.GET;
export const POST = handlers.POST;
