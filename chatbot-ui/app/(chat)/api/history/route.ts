import type {NextRequest} from "next/server";
import {cookies} from "next/headers";
import {deleteAllChatsByUserId, getChatsByUserId} from "@/lib/db/queries";
import {ChatSDKError} from "@/lib/errors";

export async function GET(request: NextRequest) {
  const { searchParams } = request.nextUrl;

  const limit = Number.parseInt(searchParams.get("limit") || "10", 10);
  const startingAfter = searchParams.get("starting_after");
  const endingBefore = searchParams.get("ending_before");

  if (startingAfter && endingBefore) {
    return new ChatSDKError(
      "bad_request:api",
      "Only one of starting_after or ending_before can be provided."
    ).toResponse();
  }

  const cookieStore = await cookies();
  const uid = cookieStore.get("RX_UID")?.value;
  if (!uid) {
    return new ChatSDKError("unauthorized:chat").toResponse();
  }

  const chats = await getChatsByUserId({
    id: uid,
    limit,
    startingAfter,
    endingBefore,
  });

  return Response.json(chats);
}

export async function DELETE() {
  const cookieStore = await cookies();
  const uid = cookieStore.get("RX_UID")?.value;
  if (!uid) {
    return new ChatSDKError("unauthorized:chat").toResponse();
  }

  const result = await deleteAllChatsByUserId({ userId: uid });

  return Response.json(result, { status: 200 });
}
