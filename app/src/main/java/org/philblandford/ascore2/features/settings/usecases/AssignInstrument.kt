package org.philblandford.ascore2.features.settings.usecases

interface AssignInstrument {
  operator fun invoke(instrument:String, group:String)
}