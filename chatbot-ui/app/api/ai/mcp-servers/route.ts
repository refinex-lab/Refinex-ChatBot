import {createCollectionHandlers} from "../_proxy";

const handlers = createCollectionHandlers("/ai/mcp-servers", "MCP Server");

export const GET = handlers.GET;
export const POST = handlers.POST;
