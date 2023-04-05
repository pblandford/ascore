package org.philblandford.ascore2.features.load.usecases

import FileInfo
import com.philblandford.kscore.engine.types.FileSource

interface GetSavedScores {
  operator fun invoke():Map<FileSource,List<FileInfo>>
}