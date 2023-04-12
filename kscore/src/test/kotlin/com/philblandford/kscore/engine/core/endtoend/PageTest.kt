package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.types.ez

import core.representation.RepTest
import org.junit.Test

class PageTest : RepTest() {


  @Test
  fun testPageFooter() {
    RVA("PageFooter", ez(1))
  }
}