package org.philblandford.ascore2.features.insert

interface InsertLyricAtMarker {
  operator fun invoke(text:String, number:Int, moveMarker:Boolean = false)
}