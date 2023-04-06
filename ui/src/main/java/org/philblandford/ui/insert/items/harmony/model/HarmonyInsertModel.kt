package org.philblandford.ui.insert.items.harmony.model

import com.philblandford.kscore.engine.pitch.Harmony
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.types.Pitch
import org.philblandford.ui.insert.model.InsertModel

data class HarmonyInsertModel(
  val current: Harmony = harmony("C")!!,
  val common: List<Harmony> = listOf(),
  val recent: List<Harmony> = listOf(),
  val notes: List<Pitch> = listOf(),
  val rootNotes: List<Pitch> = listOf(),
  val qualities: List<String> = listOf(),
) : InsertModel()

