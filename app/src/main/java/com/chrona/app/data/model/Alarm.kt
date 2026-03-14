package com.chrona.app.data.model

data class Alarm(
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(),   // Calendar.MONDAY..Calendar.SUNDAY
    val createdAt: Long = System.currentTimeMillis()
) {
    val timeString: String get() = "%02d:%02d".format(hour, minute)
}
