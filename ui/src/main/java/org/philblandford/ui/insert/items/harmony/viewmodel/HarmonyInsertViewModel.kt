package org.philblandford.ui.insert.items.harmony.viewmodel

import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.pitch.Harmony
import com.philblandford.kscore.engine.pitch.qualityNames
import com.philblandford.kscore.engine.types.Pitch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.harmony.GetHarmoniesForKey
import org.philblandford.ascore2.features.input.usecases.MoveMarker
import org.philblandford.ascore2.features.insert.GetMarker
import org.philblandford.ascore2.features.insert.InsertEventAtMarker
import org.philblandford.ascore2.features.insert.RemoveBarSplit
import org.philblandford.ascore2.features.insert.SplitBar
import org.philblandford.ascore2.features.score.ScoreUpdate
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.harmony.model.HarmonyInsertModel
import org.philblandford.ui.insert.model.InsertInterface
import timber.log.Timber

interface HarmonyInsertInterface : InsertInterface<HarmonyInsertModel> {
  fun insertCurrent()
  fun insertHarmony(harmony: Harmony)
  fun markerLeft()
  fun markerRight()
  fun split()
  fun removeSplit()
  fun setTone(tone: Pitch)
  fun setQuality(quality: String)
  fun setRoot(root: Pitch)
}

class HarmonyInsertViewModel(

  private val moveMarker: MoveMarker,
  private val getHarmoniesForKey: GetHarmoniesForKey,
  private val insertEventAtMarker: InsertEventAtMarker,
  private val splitBar: SplitBar,
  private val removeBarSplit: RemoveBarSplit,
  private val getMarker: GetMarker
) : InsertViewModel<HarmonyInsertModel, HarmonyInsertInterface>(), HarmonyInsertInterface {
  private var recentHarmonies = listOf<Harmony>()

  init {
    viewModelScope.launch {
      scoreUpdate().map { getMarker() }.distinctUntilChanged(). collectLatest { marker ->
        marker?.let {
          update { copy(common = getHarmoniesForKey()) }
        }
      }
    }
  }

  override suspend fun initState(): Result<HarmonyInsertModel> {
    Timber.e("HIVM initState")
    val common = getHarmoniesForKey()
    return HarmonyInsertModel(
      common.first(), common, recentHarmonies, Pitch.allPitches, Pitch.allPitches,
      qualityNames
    ).ok()
  }

  override fun getInterface(): HarmonyInsertInterface {
    return this
  }

  override fun insertHarmony(harmony: Harmony) {
    receiveAction { model ->
      insertEventAtMarker(harmony.toEvent()).map { model }
      model.addToRecent(harmony).ok()
    }
  }

  override fun markerLeft() {
    moveMarker(true)
  }

  override fun markerRight() {
    moveMarker(false)
  }

  override fun insertCurrent() {
    receiveAction { model ->
      insertEventAtMarker(model.current.toEvent())
      model.addToRecent(model.current).ok()
    }
  }

  override fun split() {
    splitBar()
  }

  override fun removeSplit() {
    removeBarSplit()
  }

  override fun setTone(tone: Pitch) {
    update {
      copy(current = current.copy(tone = tone))
    }
  }

  override fun setQuality(quality: String) {
    update {
      copy(current = current.copy(quality = quality))
    }
  }

  override fun setRoot(root: Pitch) {
    update {
      copy(current = current.copy(root = root))
    }
  }

  private fun HarmonyInsertModel.addToRecent(harmony: Harmony):HarmonyInsertModel {
    recentHarmonies = (recentHarmonies + harmony).reversed().distinct().reversed().takeLast(7)
    return copy(recent = recentHarmonies)
  }


}