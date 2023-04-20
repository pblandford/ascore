package org.philblandford.ui.edit.items.instrumentedit.viewmodel

import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import org.philblandford.ascore2.features.edit.MoveSelectedArea
import org.philblandford.ascore2.features.input.usecases.DeleteSelectedEvent
import org.philblandford.ascore2.features.insert.GetDefaultTextSize
import org.philblandford.ascore2.features.insert.GetInstrumentAtSelection
import org.philblandford.ascore2.features.insert.InsertEvent
import org.philblandford.ascore2.features.insert.SetInstrumentAtSelection
import org.philblandford.ascore2.features.insert.UpdateEventParam
import org.philblandford.ascore2.features.instruments.GetAvailableInstruments
import org.philblandford.ascore2.features.instruments.GetInstruments
import org.philblandford.ascore2.features.settings.usecases.GetAvailableFonts
import org.philblandford.ascore2.features.ui.usecases.GetUIState
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel

interface InstrumentEditInterface : EditInterface {
  fun instruments():List<InstrumentGroup>
  fun setInstrument(instrument:Instrument)
  fun selectedInstrument():Instrument?
}

class InstrumentEditViewModel(
  getUIState: GetUIState,
  updateEvent: UpdateEventParam,
  insertEvent: InsertEvent,
  deleteSelectedEvent: DeleteSelectedEvent,
  moveSelectedArea: MoveSelectedArea,
  private val getAvailableInstruments: GetAvailableInstruments,
  private val setInstrumentAtSelection: SetInstrumentAtSelection,
  private val getInstrumentAtSelection: GetInstrumentAtSelection
) : EditViewModel(getUIState, updateEvent, insertEvent, deleteSelectedEvent, moveSelectedArea), InstrumentEditInterface {

  override fun getInterface(): EditInterface = this

  override fun instruments(): List<InstrumentGroup> {
    return getAvailableInstruments()
  }

  override fun setInstrument(instrument: Instrument) {
    setInstrumentAtSelection(instrument)
  }

  override fun selectedInstrument(): Instrument? {
    return getInstrumentAtSelection()
  }
}