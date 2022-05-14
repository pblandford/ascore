package org.philblandford.ascore2.features.crosscutting.usecases

interface SetProgress {
  suspend operator fun invoke(yes:Boolean)
}