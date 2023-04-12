package com.philblandford.ascore

import assertEqual
import com.philblandford.kscore.util.*
import org.junit.Test
import kotlin.math.PI

class UtilTest {
  @Test
  fun testNumBytes() {
    assertEqual(4, 0xabcdef12.numBytes())
  }

  @Test
  fun testNumBytes3() {
    assertEqual(3, 0xcdef12L.numBytes())
  }

  @Test
  fun testNumBytes2() {
    assertEqual(2, 0xef12L.numBytes())
  }


  @Test
  fun testNumBytes1() {
    assertEqual(1, 0x12L.numBytes())
  }

  @Test
  fun testIsPower2_1() {
    assertEqual(true, 1.isPower2())
  }

  @Test
  fun testIsPower2_2() {
    assertEqual(true, 2.isPower2())
  }


  @Test
  fun testIsPower2_3() {
    assertEqual(false, 3.isPower2())
  }

  @Test
  fun testIsPower2_4() {
    assertEqual(true, 4.isPower2())
  }

  @Test
  fun testGCD1_1() {
    assertEqual(1, 1.gcd(1))
  }

  @Test
  fun testGCD1_3() {
    assertEqual(1, 1.gcd(3))
  }

  @Test
  fun testGCD5_3() {
    assertEqual(1, 5.gcd(3))
  }

  @Test
  fun testGCD12_8() {
    assertEqual(4, 12.gcd(8))
  }

  @Test
  fun testGCD10_45() {
    assertEqual(5, 10.gcd(45))
  }

  @Test
  fun testGCD1701_3768() {
    assertEqual(3, 1701.gcd(3768))
  }

  @Test
  fun testLCM3_4() {
    assertEqual(12, 3.lcm(4))
  }

  @Test
  fun testLCM12_8() {
    assertEqual(24, 8.lcm(12))
  }

  @Test
  fun testLCM4_2() {
    assertEqual(4, 4.lcm(2))
  }

  @Test
  fun testToDegrees() {
    assertEqual(180, PI.toFloat().toDegrees().toInt() )
  }

  @Test
  fun testToDegrees45() {
    assertEqual(45, (PI/4).toFloat().toDegrees().toInt() )
  }
}