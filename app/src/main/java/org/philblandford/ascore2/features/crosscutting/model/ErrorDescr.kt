package org.philblandford.ascore2.features.crosscutting.model

import com.philblandford.kscore.engine.core.score.Command

data class ErrorDescr(
  val headline: String,
  val message: String = "",
  val exception: Throwable? = null,
  val command:Command? = null,
  val base64:String? = null
)