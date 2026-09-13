package ai.monkmind.steady.habit

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveDaysMaskTest {

    @Test
    fun testEveryDay_containsAllSevenJavaDaysInStableBitOrder() {
        val mask = ActiveDaysMask.EveryDay

        assertEquals(127, mask.value)
        DayOfWeek.entries.forEach { day -> assertTrue(mask.contains(day)) }
        assertEquals(Weekday.entries.toSet(), mask.toWeekdays())
    }

    @Test
    fun testOf_combinesSelectedDaysWithoutDependingOnInputOrder() {
        val mask = ActiveDaysMask.of(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
        )

        assertEquals(69, mask.value)
        assertTrue(mask.contains(DayOfWeek.MONDAY))
        assertTrue(mask.contains(DayOfWeek.WEDNESDAY))
        assertTrue(mask.contains(DayOfWeek.SUNDAY))
        assertFalse(mask.contains(DayOfWeek.TUESDAY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_rejectsZero() {
        ActiveDaysMask(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructor_rejectsBitsOutsideSevenDays() {
        ActiveDaysMask(0x80)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFrom_rejectsEmptyDays() {
        ActiveDaysMask.from(emptyList())
    }
}
