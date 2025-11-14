import type {ChatModel} from "./models";

// 简化用户类型：仅保留正式用户
export type UserType = "regular";

type Entitlements = {
  maxMessagesPerDay: number;
  availableChatModelIds: ChatModel["id"][];
};

export const entitlementsByUserType: Record<UserType, Entitlements> = {
  /*
   * For users with an account
   */
  regular: {
    maxMessagesPerDay: 100,
    availableChatModelIds: ["chat-model", "chat-model-reasoning"],
  },

  /*
   * TODO: For users with an account and a paid membership
   */
};
