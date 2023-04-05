package org.philblandford.ascore2.features.page

interface GetSegmentMinMax {
  operator fun invoke():Pair<Int,Int>
}