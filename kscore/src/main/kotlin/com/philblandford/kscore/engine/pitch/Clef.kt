package com.philblandford.kscore.engine.pitch

import com.philblandford.kscore.engine.types.ClefType
import com.philblandford.kscore.engine.types.ClefType.*
import com.philblandford.kscore.engine.types.NoteLetter
import com.philblandford.kscore.engine.types.NoteLetter.*

data class Clef(val clefType: ClefType, val topNote: NoteLetter,  val topNoteOctave:Int, 
                val sharpPositions:Iterable<Int>, val flatPositions:Iterable<Int>)

private val properties = listOf(
 Clef(TREBLE, F, 5, listOf(0, 3, -1, 2, 5, 1, 4), listOf(4, 1, 5, 2, 6, 3, 7)),
 Clef(TREBLE_8VA, F, 6, listOf(0, 3, -1, 2, 5, 1, 4), listOf(4, 1, 5, 2, 6, 3, 7)),
 Clef(TREBLE_8VB, F, 4, listOf(0, 3, -1, 2, 5, 1, 4), listOf(4, 1, 5, 2, 6, 3, 7)),
 Clef(BASS, A, 3, listOf(2, 5, 1, 4, 7, 3, 5), listOf(6, 3, 7, 4, 8, 5, 9)),
 Clef(BASS_8VA, A, 4, listOf(2, 5, 1, 4, 7, 3, 5), listOf(6, 3, 7, 4, 8, 5, 9)),
 Clef(BASS_8VB, A, 2, listOf(2, 5, 1, 4, 7, 3, 5), listOf(6, 3, 7, 4, 8, 5, 9)),
 Clef(SOPRANO, D, 5, listOf(5, 1, 4, 0, 3, -1, 2), listOf(2, 6, 3, 7, 4, 8, 5)),
 Clef(MEZZO, B, 4, listOf(3, 6, 2, 5, 1, 4, 0), listOf(0, 4, 1, 5, 2, 6, 3)),
 Clef(ALTO, G, 4, listOf(1, 4, 0, 3, 6, 2, 4), listOf(5, 2, 6, 3, 7, 4, 8)),
 Clef(TENOR, E, 4, listOf(6, 2, 5, 1, 4, 0, 3), listOf(3, 0, 4, 1, 5, 2, 6))
)
private val propertiesGrouped = properties.groupBy { it.clefType }

fun getClef(clefType: ClefType):Clef? {
    return propertiesGrouped[clefType]?.firstOrNull()
}
