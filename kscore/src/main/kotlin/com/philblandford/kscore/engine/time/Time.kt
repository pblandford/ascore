package com.philblandford.kscore.engine.time

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import com.philblandford.kscore.util.isPower2
import org.apache.commons.math3.fraction.Fraction

data class TimeSignature(
    val numerator: Int, val denominator: Int,
    val type: TimeSignatureType = TimeSignatureType.CUSTOM,
    val hidden: Boolean = false
) {
    init {
        if (denominator == 0) {
            ksLoge("denominator 0")
        }
    }

    val duration = Duration(numerator, denominator)

    fun toHiddenEvent(): Event = toEvent().copy(eventType = EventType.HIDDEN_TIME_SIGNATURE)

    fun toEvent(): Event {
        val (num, den) = type.toNumDen()

        return Event(
            EventType.TIME_SIGNATURE,
            paramMapOf(
                EventParam.NUMERATOR to num,
                EventParam.DENOMINATOR to den,
                EventParam.TYPE to type,
                EventParam.HIDDEN to hidden
            )
        )
    }

    fun isValid(): Boolean {
        return numerator >= 1 && denominator > 0 && denominator.isPower2() && denominator <= 128
    }

    fun divide(num: Int): TimeSignature {
        val fraction = Fraction(numerator, denominator).divide(num)
        return TimeSignature(fraction.numerator, fraction.denominator)
    }

    fun setType(type: TimeSignatureType): TimeSignature {
        val (num, den) = type.toNumDen()
        return copy(numerator = num, denominator = den, type = type)
    }

    private fun TimeSignatureType.toNumDen(): Pair<Int, Int> {
        return when (this) {
            TimeSignatureType.COMMON -> 4 to 4
            TimeSignatureType.CUT_COMMON -> 2 to 2
            else -> numerator to denominator
        }
    }

    companion object {
        fun common() = TimeSignature(4, 4, TimeSignatureType.COMMON)
        fun cutCommon() = TimeSignature(2, 2, TimeSignatureType.CUT_COMMON)
        fun custom(numerator: Int, denominator: Int) =
            TimeSignature(numerator, denominator, TimeSignatureType.CUSTOM)

        fun fromParams(params: ParamMap): TimeSignature {
            return TimeSignature(
                params.g<Int>(EventParam.NUMERATOR) ?: 4,
                params.g<Int>(EventParam.DENOMINATOR) ?: 4,
                params.g<TimeSignatureType>(EventParam.TYPE) ?: TimeSignatureType.CUSTOM,
                params.g<Boolean>(EventParam.HIDDEN) ?: false,
            )
        }

    }

}

fun timeSignature(event: Event): TimeSignature? {
    event.params[EventParam.NUMERATOR]?.let {
    } ?: run {
        ksLoge("OOps")
    }
    val num = event.params[EventParam.NUMERATOR] as Int
    val den = event.params[EventParam.DENOMINATOR] as Int
    val type = event.subType as TimeSignatureType
    val hidden = event.params.get(EventParam.HIDDEN) as Boolean? ?: false
    return TimeSignature(num, den, type, hidden)
}


fun TimeSignature.getRegularDivisions(): List<Pair<Offset, Duration>> {
    if (numerator == 1 || numerator % 2 == 0 || numerator % 3 == 0) {
        return listOf(dZero() to duration)
    }
    val first = dZero() to Duration(numerator / 2 + 1, denominator)
    val second = first.second to Duration(numerator / 2, denominator)
    return listOf(first, second)
}
