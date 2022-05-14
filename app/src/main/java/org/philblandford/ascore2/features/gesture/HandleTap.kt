package org.philblandford.ascore2.features.gesture

interface HandleTap {
  operator fun invoke(page:Int, x:Int, y:Int)
}