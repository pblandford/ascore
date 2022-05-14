package org.philblandford.ascore2.features.clipboard.usecases

import com.philblandford.kscore.api.KScore

class PasteImpl(private val kScore: KScore, private val clearSelection: ClearSelection) : Paste {
  override operator fun invoke() {
    kScore.paste()
    clearSelection()
  }
}