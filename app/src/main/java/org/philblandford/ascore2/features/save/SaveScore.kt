package org.philblandford.ascore2.features.save

import com.philblandford.kscore.engine.types.FileSource

interface SaveScore {
  operator fun invoke(name:String, fileSource: FileSource):Result<Unit>
}