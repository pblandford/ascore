package org.philblandford.ui.insert.items.segmentwidth.model

import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.insert.model.InsertModel

data class SegmentWidthModel(val current:Int, val min:Int, val max:Int) : InsertModel()
