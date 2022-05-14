package org.philblandford.ascore2.features.gesture

interface HandleLongPress {
  operator fun invoke(page:Int, x:Int, y:Int)
}