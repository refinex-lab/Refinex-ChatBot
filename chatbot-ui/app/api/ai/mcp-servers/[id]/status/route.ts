import {createStatusHandlers} from "../../../_proxy";

const handlers = createStatusHandlers("/ai/mcp-servers", "MCP Server");

export const PATCH = handlers.PATCH;
