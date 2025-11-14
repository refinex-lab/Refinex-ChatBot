import {cookies} from "next/headers";
import {getSuggestionsByDocumentId} from "@/lib/db/queries";
import {ChatSDKError} from "@/lib/errors";

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const documentId = searchParams.get("documentId");

  if (!documentId) {
    return new ChatSDKError(
      "bad_request:api",
      "Parameter documentId is required."
    ).toResponse();
  }

  const cookieStore = await cookies();
  const uid = cookieStore.get("RX_UID")?.value;
  if (!uid) {
    return new ChatSDKError("unauthorized:suggestions").toResponse();
  }

  const suggestions = await getSuggestionsByDocumentId({
    documentId,
  });

  const [suggestion] = suggestions;

  if (!suggestion) {
    return Response.json([], { status: 200 });
  }

  if (suggestion.userId !== uid) {
    return new ChatSDKError("forbidden:api").toResponse();
  }

  return Response.json(suggestions, { status: 200 });
}
