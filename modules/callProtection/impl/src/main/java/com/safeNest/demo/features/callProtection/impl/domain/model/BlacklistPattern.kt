package com.safeNest.demo.features.callProtection.impl.domain.model

import com.safeNest.demo.features.callProtection.impl.data.local.BlacklistPatternEntity

data class BlacklistPattern(
    val pattern: String,
    val description: String
) {
    fun toBlacklistPatternEntity(): BlacklistPatternEntity {
        return BlacklistPatternEntity(pattern, description)
    }
}