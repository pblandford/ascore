package duration

import assertEqual
import com.philblandford.kscore.engine.duration.*
import org.junit.Test

class DurationTest {

  @Test
  fun testNumDots() {
    assertEqual(0, crotchet().numDots())
  }

  @Test
  fun testNumDots1() {
    assertEqual(1, crotchet(1).numDots())
  }

  @Test
  fun testNumDots2() {
    assertEqual(2, crotchet(2).numDots())
  }

  @Test
  fun testNumDotsLonga() {
    assertEqual(0, longa().numDots())
  }

  @Test
  fun testNumDotsLonga1() {
    assertEqual(1, longa(1).numDots())
  }

  @Test
  fun testNumDotsLonga2() {
    assertEqual(2, longa(2).numDots())
  }

  @Test
  fun testDot0() {
    assertEqual(Duration(1,4), crotchet().dot(0))
  }

  @Test
  fun testDot1() {
    assertEqual(Duration(3,8), crotchet().dot(1))
  }

  @Test
  fun testDot2() {
    assertEqual(Duration(7,16), crotchet().dot(2))
  }

  @Test
  fun testDotSemibreve() {
    assertEqual(Duration(3, 2), semibreve().dot(1))
  }

  @Test
  fun testDotBreve() {
    assertEqual(Duration(3, 1), breve().dot(1))
  }

  @Test
  fun testUnDot() {
    assertEqual(Duration(1, 4), crotchet().dot(1).undot())
  }

  @Test
  fun testUnDotCrotchet() {
    assertEqual(Duration(1,4), crotchet().undot())
  }

  @Test
  fun testUnDotDottedSemibreve() {
    assertEqual(Duration(1,1), semibreve().dot(1).undot())
  }

  @Test
  fun testUnDotDoubleDottedCrotchet() {
    assertEqual(Duration(1,4), crotchet(2).undot())
  }

  @Test
  fun testUnDotBreve() {
    assertEqual(Duration(2,1), breve().undot())
  }

  @Test
  fun testUnDotDottedBreve() {
    assertEqual(Duration(2,1), breve(1).undot())
  }

  @Test
  fun testReduceDots() {
    assertEqual(12, 13.reduceDots(1))
  }

  @Test
  fun testReduceDots5() {
    assertEqual(4, 5.reduceDots(0))
  }
}