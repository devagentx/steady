package ai.monkmind.steady.habit

object HabitCatalog {
    const val LEAF_ICON_ID = "leaf"
    const val DROP_ICON_ID = "drop"
    const val DUMBBELL_ICON_ID = "dumbbell"
    const val BOTTLE_ICON_ID = "bottle"
    const val STEPS_ICON_ID = "steps"

    const val SAGE_COLOR_ID = "sage"
    const val BLUE_COLOR_ID = "blue"
    const val LILAC_COLOR_ID = "lilac"
    const val WARM_COLOR_ID = "warm"
    const val NEUTRAL_COLOR_ID = "neutral"

    const val MOUNTAIN_PROFILE_ICON_ID = "mountain"
    const val MOON_PROFILE_ICON_ID = "moon"
    const val SUN_PROFILE_ICON_ID = "sun"
    const val LOTUS_PROFILE_ICON_ID = "lotus"

    val habitIconIds: Set<String> = linkedSetOf(
        DROP_ICON_ID,
        DUMBBELL_ICON_ID,
        BOTTLE_ICON_ID,
        STEPS_ICON_ID,
        LEAF_ICON_ID,
    )

    val habitColorIds: Set<String> = linkedSetOf(
        BLUE_COLOR_ID,
        SAGE_COLOR_ID,
        LILAC_COLOR_ID,
        WARM_COLOR_ID,
        NEUTRAL_COLOR_ID,
    )

    val profileIconIds: Set<String> = linkedSetOf(
        LEAF_ICON_ID,
        MOUNTAIN_PROFILE_ICON_ID,
        MOON_PROFILE_ICON_ID,
        SUN_PROFILE_ICON_ID,
        LOTUS_PROFILE_ICON_ID,
    )

    val styles: List<HabitCatalogStyle> = listOf(
        HabitCatalogStyle(DROP_ICON_ID, BLUE_COLOR_ID),
        HabitCatalogStyle(DUMBBELL_ICON_ID, SAGE_COLOR_ID),
        HabitCatalogStyle(BOTTLE_ICON_ID, LILAC_COLOR_ID),
        HabitCatalogStyle(STEPS_ICON_ID, WARM_COLOR_ID),
        HabitCatalogStyle(LEAF_ICON_ID, NEUTRAL_COLOR_ID),
    )

    fun containsHabitIcon(id: String): Boolean = id in habitIconIds

    fun containsHabitColor(id: String): Boolean = id in habitColorIds

    fun containsProfileIcon(id: String): Boolean = id in profileIconIds

    fun suggestedStyle(name: String): HabitCatalogStyle {
        val normalizedName = name.trim().lowercase()
        return when {
            WATER_KEYWORDS.any(normalizedName::contains) ->
                HabitCatalogStyle(DROP_ICON_ID, BLUE_COLOR_ID)
            EXERCISE_KEYWORDS.any(normalizedName::contains) ->
                HabitCatalogStyle(DUMBBELL_ICON_ID, SAGE_COLOR_ID)
            WELLNESS_KEYWORDS.any(normalizedName::contains) ->
                HabitCatalogStyle(BOTTLE_ICON_ID, LILAC_COLOR_ID)
            WALKING_KEYWORDS.any(normalizedName::contains) ->
                HabitCatalogStyle(STEPS_ICON_ID, WARM_COLOR_ID)
            else -> HabitCatalogStyle(LEAF_ICON_ID, NEUTRAL_COLOR_ID)
        }
    }

    private val WATER_KEYWORDS = listOf("water", "hydrate", "drink")
    private val EXERCISE_KEYWORDS = listOf("exercise", "gym", "workout", "lift", "strength")
    private val WELLNESS_KEYWORDS =
        listOf("supplement", "vitamin", "medicine", "medication", "pill")
    private val WALKING_KEYWORDS = listOf("walk", "walking", "steps", "hike")
}

data class HabitCatalogStyle(
    val iconId: String,
    val colorId: String,
)
