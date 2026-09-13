package ai.monkmind.steady.habit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitDraftValidationTest {

    @Test
    fun testValidate_trimsNameAndNoteWithoutApplyingLengthLimits() {
        val longNote = "x".repeat(500)
        val result = validDraft(
            name = "  Morning walk  ",
            goalNote = "  $longNote  ",
        ).validate()

        val valid = result as HabitDraftValidationResult.Valid
        assertEquals("Morning walk", valid.draft.name)
        assertEquals(longNote, valid.draft.goalNote)
    }

    @Test
    fun testValidate_mapsBlankNoteToNull() {
        val result = validDraft(goalNote = " \n ").validate()

        val valid = result as HabitDraftValidationResult.Valid
        assertNull(valid.draft.goalNote)
    }

    @Test
    fun testValidate_reportsEveryInvalidMutableField() {
        val result = HabitDraft(
            name = " ",
            goalNote = null,
            activeDays = null,
            schedule = null,
            iconId = "unknown-icon",
            colorId = "unknown-color",
        ).validate()

        assertEquals(
            linkedSetOf(
                HabitDraftError.BlankName,
                HabitDraftError.MissingActiveDays,
                HabitDraftError.MissingSchedule,
                HabitDraftError.UnknownIcon,
                HabitDraftError.UnknownColor,
            ),
            (result as HabitDraftValidationResult.Invalid).errors,
        )
    }

    @Test
    fun testR0Default_usesAllDaysAnytimeAndPreservedLeafSageIds() {
        val result = HabitDraft.r0Default("  Read  ").validate()

        val draft = (result as HabitDraftValidationResult.Valid).draft
        assertEquals("Read", draft.name)
        assertEquals(127, draft.activeDays.value)
        assertEquals(HabitSchedule.RoutineCue(Cue.Anytime), draft.schedule)
        assertEquals("leaf", draft.iconId)
        assertEquals("sage", draft.colorId)
    }

    @Test
    fun testExactTime_acceptsMinuteBounds() {
        assertEquals(0, HabitSchedule.ExactTime(0).minuteOfDay)
        assertEquals(1439, HabitSchedule.ExactTime(1439).minuteOfDay)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExactTime_rejectsNegativeMinute() {
        HabitSchedule.ExactTime(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExactTime_rejectsMinuteAfterEndOfDay() {
        HabitSchedule.ExactTime(1440)
    }

    @Test
    fun testCuePersistedIds_areStableAndRoundTrip() {
        val expected = listOf(
            "ANYTIME",
            "AFTER_WAKING",
            "AFTER_BREAKFAST",
            "AFTER_LUNCH",
            "AFTER_DINNER",
            "BEFORE_BED",
        )

        assertEquals(expected, Cue.entries.map(Cue::persistedId))
        expected.forEach { id -> assertTrue(Cue.fromPersistedId(id) != null) }
        assertNull(Cue.fromPersistedId("UNKNOWN"))
    }

    private fun validDraft(
        name: String = "Morning walk",
        goalNote: String? = null,
    ) = HabitDraft(
        name = name,
        goalNote = goalNote,
        activeDays = ActiveDaysMask.EveryDay,
        schedule = HabitSchedule.Anytime,
        iconId = DEFAULT_HABIT_ICON_ID,
        colorId = DEFAULT_HABIT_COLOR_ID,
    )
}
