package org.philblandford.ui.insert.items.harmony.viewmodel

import com.philblandford.kscore.engine.pitch.Harmony
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.harmony.GetHarmoniesForKey
import org.philblandford.ascore2.features.input.usecases.MoveMarker
import org.philblandford.ascore2.features.insert.InsertEventAtMarker
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertParams
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.harmony.model.HarmonyInsertModel
import org.philblandford.ui.insert.model.InsertInterface

interface HarmonyInsertInterface : InsertInterface<HarmonyInsertModel> {
  fun insertCurrent()
  fun insertHarmony(harmony: Harmony)
  fun markerLeft()
  fun markerRight()
  fun split()
  fun removeSplit()
  fun setTone(tone:String)
  fun setQuality(quality:String)
  fun setRoot(root:String)
}

class HarmonyInsertViewModel(

  private val moveMarker: MoveMarker,
  private val getHarmoniesForKey: GetHarmoniesForKey,
  private val insertEventAtMarker: InsertEventAtMarker
) : InsertViewModel<HarmonyInsertModel, HarmonyInsertInterface>(), HarmonyInsertInterface {

  override suspend fun initState(): Result<HarmonyInsertModel> {
    val common = getHarmoniesForKey()
    return HarmonyInsertModel(common.first(), common, listOf()).ok()
  }

  override fun getInterface(): HarmonyInsertInterface {
    return this
  }

  override fun insertHarmony(harmony: Harmony) {
    receiveAction { model ->
        insertEventAtMarker(harmony.toEvent()).map { model }
    }
  }


  override fun markerLeft() {
    moveMarker(true)
  }

  override fun markerRight() {
    moveMarker(false)
  }

  override fun insertCurrent() {
    getState().value?.let { model ->
      insertEventAtMarker(model.current.toEvent())
    }
  }

  override fun split() {
    TODO("Not yet implemented")
  }

  override fun removeSplit() {
    TODO("Not yet implemented")
  }

  override fun setTone(tone: String) {
    TODO("Not yet implemented")
  }

  override fun setQuality(quality: String) {
    TODO("Not yet implemented")
  }

  override fun setRoot(root: String) {
    TODO("Not yet implemented")
  }
}