package org.philblandford.ui.insert.items.pagesize.model

import org.philblandford.ui.base.viewmodel.VMModel

data class PageSizeModel(val currentSize:Int, val minSize:Int, val maxSize:Int,
val step:Int) : VMModel()
