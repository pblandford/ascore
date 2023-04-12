package core.geographyX

import com.philblandford.kscore.engine.types.StaveId
import com.philblandford.kscore.engine.types.ea
import assertEqual
import com.philblandford.kscore.engine.core.*
import com.philblandford.kscore.engine.types.eas
import com.philblandford.kscore.engine.core.geographyX.alignSegments
import com.philblandford.kscore.engine.core.geographyX.getPadding
import com.philblandford.kscore.engine.core.representation.TADPOLE_WIDTH
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class BarSegmentAlignerTest {

  val WIDTH = TADPOLE_WIDTH
  val XMARGIN = 20
  val PADDED = WIDTH + getPadding(crotchet())
  fun PADDED(duration: Duration = crotchet()) = WIDTH + getPadding(duration)

  @Test
  fun testAlignOneSegment() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val offsetMap = alignSegments(map)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED), offsetMap.toList().first().second)
  }

  @Test
  fun testAlignTwoSegments() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf()),
        ea(1, crotchet()) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )

    val offsetMap = alignSegments(map)!!
    assertEqual(2, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED), offsetMap.toList().first().second)
    assertEqual(SlicePosition(PADDED, PADDED, PADDED), offsetMap.toList()[1].second)
  }

  @Test
  fun testAlignFourSegments() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf()),
        ea(1, crotchet()) to SegmentGeography(WIDTH, 0, crotchet(), mapOf()),
        ea(1, minim()) to SegmentGeography(WIDTH, 0, crotchet(), mapOf()),
        ea(1, minim(1)) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )

    val offsetMap = alignSegments(map)!!.toList().sortedBy { it.first }
    assertEqual(4, offsetMap.size)
    for (i in 0..3) {
      assertEqual(
        SlicePosition(PADDED * i, PADDED * i, PADDED),
        offsetMap.toList()[i].second
      )

    }
  }

  @Test
  fun testAlignOneSegmentWithXMargin() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(
          WIDTH + XMARGIN, XMARGIN, crotchet(),
          mapOf(1 to VoiceGeography(WIDTH + XMARGIN, XMARGIN, crotchet(), null, listOf()))
        )
      )
    )
    val offsetMap = alignSegments(map)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, XMARGIN, PADDED + XMARGIN), offsetMap.toList().first().second)
  }

  @Test
  fun testAlignOneSegmentWithTextWidth() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val textWidths = mapOf(dZero() to 1000)
    val offsetMap = alignSegments(map, textWidths)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, 0, 1000), offsetMap.toList().first().second)
  }

  @Test
  fun testAlignOneSegmentHarmonyWidthNoCollision() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf()),
        ea(1, crotchet()) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val textWidths = mapOf(dZero() to 100)
    val offsetMap = alignSegments(map, harmonyWidths = textWidths)!!
    assertEqual(2, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED), offsetMap.toList()[0].second)
    assertEqual(SlicePosition(PADDED, PADDED, PADDED), offsetMap.toList()[1].second)
  }

  @Test
  fun testAlignOneSegmentHarmonyWidthCollision() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf()),
        ea(1, crotchet()) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val textWidths = mapOf(dZero() to PADDED * 2, crotchet() to PADDED)
    val offsetMap = alignSegments(map, harmonyWidths = textWidths)!!
    assertEqual(2, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED * 2), offsetMap.toList()[0].second)
    assertEqual(SlicePosition(PADDED * 2, PADDED * 2, PADDED), offsetMap.toList()[1].second)
  }


  @Test
  fun testAlignOneSegmentTwoStaves() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      ),
      StaveId(2, 1) to mapOf(
        eas(1, 2, 1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val offsetMap = alignSegments(map)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED), offsetMap.toList().first().second)
  }

  @Test
  fun testAlignOneSegmentTwoStavesXMargin() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, XMARGIN, crotchet(), mapOf())
      ),
      StaveId(2, 1) to mapOf(
        eas(1, 2, 1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val offsetMap = alignSegments(map)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, XMARGIN, PADDED + XMARGIN), offsetMap.toList().first().second)
  }

  @Test
  fun testAlignSegmentsTwoStavesRoomForXMargin() {
    val offsetMap = smap(
      listOf(
        SegmentGeography(WIDTH, 0, crotchet()),
        SegmentGeography(WIDTH, 0, crotchet()),
        SegmentGeography(WIDTH, 0, crotchet()),
        SegmentGeography(WIDTH, 0, crotchet())
      ),
      listOf(
        SegmentGeography(WIDTH, 0, minim()),
        null,
        SegmentGeography(WIDTH, XMARGIN, minim()),
        null
      )
    )
    assertEqual(4, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED), offsetMap.toList()[0].second)
    assertEqual(SlicePosition(PADDED, PADDED, PADDED), offsetMap.toList()[1].second)
    assertEqual(
      SlicePosition(PADDED * 2, PADDED * 2, PADDED),
      offsetMap.toList()[2].second
    )
    assertEqual(
      SlicePosition(PADDED * 3, PADDED * 3, PADDED),
      offsetMap.toList()[3].second
    )
  }

  @Test
  fun testAlignSegmentsTwoStavesDifferentOffsets() {
    val offsetMap = smap(
      listOf(
        SegmentGeography(WIDTH, 0, crotchet()),
        SegmentGeography(WIDTH, 0, minim()),
        null,
        SegmentGeography(WIDTH, 0, crotchet())
      ),
      listOf(
        SegmentGeography(WIDTH, 0, minim()),
        null,
        SegmentGeography(WIDTH, 0, minim()),
        null
      )
    )
    assertEqual(4, offsetMap.size)
    assertEqual(SlicePosition(0, 0, PADDED), offsetMap.toList()[0].second)
    assertEqual(SlicePosition(PADDED, PADDED, PADDED), offsetMap.toList()[1].second)
    assertEqual(
      SlicePosition(PADDED * 2, PADDED * 2, PADDED),
      offsetMap.toList()[2].second
    )
    assertEqual(
      SlicePosition(PADDED * 3, PADDED * 3, PADDED),
      offsetMap.toList()[3].second
    )
  }

  @Test
  fun testAlignOneSegmentWithFermataWidth() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val fermataWidths = mapOf(dZero() to 500)
    val offsetMap = alignSegments(map, fermataWidths = fermataWidths)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, 0, 500), offsetMap.toList().first().second)
  }

  @Test
  fun testAlignOneSegmentWithGlissandoWidth() {
    val map = mapOf(
      StaveId(1, 1) to mapOf(
        ea(1) to SegmentGeography(WIDTH, 0, crotchet(), mapOf())
      )
    )
    val glissandoWidths = mapOf(dZero() to 500)
    val offsetMap = alignSegments(map, segmentExtensionWidths = glissandoWidths)!!
    assertEqual(1, offsetMap.size)
    assertEqual(SlicePosition(0, 0, 500), offsetMap.toList().first().second)
  }


  private fun smap(
    stave1: Iterable<SegmentGeography?>,
    stave2: Iterable<SegmentGeography?>
  ): HorizontalMap {
    val map = mapOf(
      StaveId(1, 1) to stave1.withIndex().mapNotNull { iv ->
        iv.value?.let { ea(1, crotchet() * iv.index) to it }
      }.toMap(),
      StaveId(2, 1) to stave2.withIndex().mapNotNull { iv ->
        iv.value?.let { eas(1, crotchet() * iv.index, StaveId(2, 1)) to it }
      }.toMap()
    )
    return alignSegments(map)!!
  }
}