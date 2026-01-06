package sim2.app.talleb_5edma.util

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long): Date?{
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}
