package com.safeNest.features.core.authChallenge.impl.presentation.activity

import androidx.lifecycle.ViewModel
import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import com.safeNest.features.core.authChallenge.impl.JSON
import com.safeNest.features.core.authChallenge.impl.domain.useCase.toAuthChallenge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

@HiltViewModel
internal class AuthChallengeViewModel @Inject constructor() : ViewModel() {

    var authChallenge: AuthChallenge = AuthChallenge.EMPTY

    fun initAuthChallenge(jsonString: String?) {
        if (jsonString.isNullOrEmpty()) throw IllegalArgumentException("jsonString is empty")
        authChallenge = toAuthChallenge(JSON.parseToJsonElement(jsonString).jsonObject)
    }
}