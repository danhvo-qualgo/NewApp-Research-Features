package net.qualgo.safeNest.core.authChallenge.impl.domain.model

object ClientMetadataKey {
    const val EMAIL = "email"
    const val CODE = "code"
    const val ID_TOKEN = "idToken"
    const val TYPE = "type"

    const val MOCK_NEXT_CHALLENGE_NAME = "mockNextChallengeName"
}

object ClientMetadataValue {
    const val SIGN_IN_TYPE_EMAIL = "EMAIL"
    const val SIGN_IN_TYPE_GOOGLE = "GOOGLE"
}