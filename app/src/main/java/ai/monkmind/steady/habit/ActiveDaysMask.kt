package ai.monkmind.steady.habit

import java.time.DayOfWeek

enum class Weekday(
    val dayOfWeek: DayOfWeek,
    val bit: Int,
) {
    Monday(DayOfWeek.MONDAY, 1 shl 0),
    Tuesday(DayOfWeek.TUESDAY, 1 shl 1),
    Wednesday(DayOfWeek.WEDNESDAY, 1 shl 2),
    Thursday(DayOfWeek.THURSDAY, 1 shl 3),
    Friday(DayOfWeek.FRIDAY, 1 shl 4),
    Saturday(DayOfWeek.SATURDAY, 1 shl 5),
    Sunday(DayOfWeek.SUNDAY, 1 shl 6),
    ;

    companion object {
        fun from(day: DayOfWeek): Weekday =
            entries.first { weekday -> weekday.dayOfWeek == day }
    }
}

@JvmInline
value class ActiveDaysMask(val value: Int) {
    init {
        require(value != 0) { "At least one active day is required." }
        require(value and ALL_DAYS_VALUE == value) {
            "Active day masks may contain only Monday through Sunday."
        }
    }

    fun contains(day: DayOfWeek): Boolean = contains(Weekday.from(day))

    fun contains(weekday: Weekday): Boolean = value and weekday.bit != 0

    fun toWeekdays(): Set<Weekday> =
        Weekday.entries.filterTo(linkedSetOf(), ::contains)

    companion object {
        const val ALL_DAYS_VALUE = 0x7F

        val EveryDay = ActiveDaysMask(ALL_DAYS_VALUE)

        fun of(vararg days: DayOfWeek): ActiveDaysMask = from(days.asIterable())

        fun from(days: Iterable<DayOfWeek>): ActiveDaysMask {
            val value = days.fold(0) { mask, day -> mask or Weekday.from(day).bit }
            return ActiveDaysMask(value)
        }
    }
}
