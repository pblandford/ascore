package org.philblandford.ascore2.features.drawing

interface Redraw {
  operator fun invoke(pages:List<Int> = listOf())
}