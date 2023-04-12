package com.philblandford.kscore.engine.core.score

import assertEqual
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.times
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class TupletTest {

  @Test
  fun testCreateTupletWithDuration() {
    val tuplet = tuplet(dZero(), 3, crotchet())
    assertEqual(TimeSignature(3,8), tuplet.timeSignature)
  }

  @Test
  fun testCreateSextuplet() {
    val tuplet = tuplet(dZero(), 6, crotchet())
    assertEqual(TimeSignature(6,16), tuplet.timeSignature)
  }

  @Test
  fun testCreateTwoFrom3() {
    val tuplet = tuplet(dZero(), 2, minim(1))
    assertEqual(TimeSignature(2,4), tuplet.timeSignature)
  }

  @Test
  fun testCreate4From3() {
    val tuplet = tuplet(dZero(), 4,  crotchet(1))
    assertEqual(TimeSignature(4,16), tuplet.timeSignature)
  }

  @Test
  fun testCreate7From5() {
    val tuplet = tuplet(dZero(), 7,  crotchet()*5)
    assertEqual(TimeSignature(7,4), tuplet.timeSignature)
  }

}