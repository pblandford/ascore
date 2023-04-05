package org.philblandford.ascore2.features.load.usecases

import FileInfo

interface DeleteScore {
  operator fun invoke(fileInfo: FileInfo)
}