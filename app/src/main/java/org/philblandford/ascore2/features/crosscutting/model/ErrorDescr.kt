package org.philblandford.ascore2.features.crosscutting.model

data class ErrorDescr(
  val headline: String,
  val message: String = "",
  val exception: Throwable? = null
)