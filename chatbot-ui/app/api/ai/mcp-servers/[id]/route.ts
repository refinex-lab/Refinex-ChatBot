import {createEntityHandlers} from "../../_proxy";

const handlers = createEntityHandlers("/ai/mcp-servers", "MCP Server");

export const GET = handlers.GET;
export const PUT = handlers.PUT;
export const DELETE = handlers.DELETE;
