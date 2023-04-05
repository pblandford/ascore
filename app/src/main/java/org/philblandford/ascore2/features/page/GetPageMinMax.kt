package org.philblandford.ascore2.features.page

interface GetPageMinMax {
  operator fun invoke():Pair<Int,  Int>
}