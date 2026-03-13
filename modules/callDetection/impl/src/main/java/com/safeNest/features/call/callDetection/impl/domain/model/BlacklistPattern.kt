
import com.safeNest.features.call.callDetection.impl.data.local.BlacklistPatternEntity

data class BlacklistPattern(
    val pattern: String,
    val description: String
) {
    fun toBlacklistPatternEntity(): BlacklistPatternEntity {
        return BlacklistPatternEntity(pattern, description)
    }
}