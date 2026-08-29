package com.example.nova.data

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromDaySet(days: Set<DayOfWeek>): String =
        days.joinToString(",") { it.value.toString() }

    @TypeConverter
    fun toDaySet(data: String): Set<DayOfWeek> =
        if (data.isBlank()) emptySet()
        else data.split(",").filter { it.isNotBlank() }.map { DayOfWeek.of(it.toInt()) }.toSet()

    @TypeConverter
    fun fromCategory(category: TaskCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): TaskCategory = TaskCategory.valueOf(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
