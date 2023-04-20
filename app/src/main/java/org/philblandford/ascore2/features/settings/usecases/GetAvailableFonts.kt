package org.philblandford.ascore2.features.settings.usecases

interface GetAvailableFonts {
    operator fun invoke():List<String>
}