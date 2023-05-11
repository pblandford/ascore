package org.philblandford.ascore2.features.load.usecases

import android.net.Uri
import com.philblandford.kscore.api.ProgressFunc
import java.net.URI

interface ImportScore {
  suspend operator fun invoke(uri: Uri, progress:(String,String,String,Float)->Unit):String?
}