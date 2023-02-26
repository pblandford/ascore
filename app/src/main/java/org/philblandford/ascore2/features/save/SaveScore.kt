package org.philblandford.ascore2.features.save

interface SaveScore {
  operator fun invoke(name:String):Result<Unit>
}