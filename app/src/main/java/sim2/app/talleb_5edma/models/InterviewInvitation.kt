package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

/**
 * Interview Invitation data model
 */
data class InterviewInvitation(
    @SerializedName("invitation_id") val invitationId: Int,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("from_user_id") val fromUserId: String,
    @SerializedName("to_user_id") val toUserId: String,
    @SerializedName("from_user_name") val fromUserName: String,
    @SerializedName("offer_id") val offerId: String? = null,
    @SerializedName("status") val status: String = "pending", // pending, accepted, rejected
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("responded_at") val respondedAt: String? = null
)

/**
 * Request to send interview invitation
 */
data class SendInterviewInvitationRequest(
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("from_user_id") val fromUserId: String,
    @SerializedName("to_user_id") val toUserId: String,
    @SerializedName("from_user_name") val fromUserName: String,
    @SerializedName("offer_id") val offerId: String? = null
)

/**
 * Response from sending invitation
 */
data class SendInterviewInvitationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("invitation_id") val invitationId: Int? = null,
    @SerializedName("chat_id") val chatId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("error") val error: String? = null
)

/**
 * Response from getting pending invitations
 */
data class GetPendingInvitationsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("invitations") val invitations: List<InterviewInvitation> = emptyList(),
    @SerializedName("count") val count: Int = 0,
    @SerializedName("error") val error: String? = null
)

/**
 * Request to accept/reject invitation
 */
data class RespondToInvitationRequest(
    @SerializedName("invitation_id") val invitationId: Int
)

/**
 * Response from accepting invitation
 */
data class AcceptInvitationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("invitation") val invitation: InvitationDetails? = null,
    @SerializedName("error") val error: String? = null
)

/**
 * Invitation details in response
 */
data class InvitationDetails(
    @SerializedName("invitation_id") val invitationId: Int,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("from_user_id") val fromUserId: String,
    @SerializedName("from_user_name") val fromUserName: String,
    @SerializedName("offer_id") val offerId: String? = null
)

/**
 * Response from rejecting invitation
 */
data class RejectInvitationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)
