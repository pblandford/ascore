package org.philblandford.ascore2.features.load.usecases

import android.net.Uri
import java.net.URI

interface ImportScore {
  operator fun invoke(uri: Uri, progress:(Float)->Unit = {})
}