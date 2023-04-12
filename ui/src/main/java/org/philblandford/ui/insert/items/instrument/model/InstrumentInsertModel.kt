package org.philblandford.ui.insert.items.instrument.model

import com.philblandford.kscore.api.InstrumentGroup
import org.philblandford.ui.insert.model.InsertModel

data class InstrumentInsertModel(val instrumentGroups:List<InstrumentGroup>) : InsertModel()
