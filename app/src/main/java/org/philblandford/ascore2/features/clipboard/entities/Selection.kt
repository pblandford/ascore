package org.philblandford.ascore2.features.clipboard.entities

import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.EventAddress

data class Selection(val start:EventAddress, val end:EventAddress?, val startLocation:Location?,
val endLocation: Location?)
