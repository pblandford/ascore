package org.philblandford.ascore2.features.load.usecases

interface GetSavedScores {
  operator fun invoke():List<String>
}