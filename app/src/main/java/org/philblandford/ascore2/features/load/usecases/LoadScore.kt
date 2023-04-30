package org.philblandford.ascore2.features.load.usecases

import com.philblandford.kscore.api.ProgressFunc
import com.philblandford.kscore.engine.types.FileSource

interface LoadScore {
  operator fun invoke(name:String, source:FileSource, progress:ProgressFunc = {_,_,_ -> false})
}