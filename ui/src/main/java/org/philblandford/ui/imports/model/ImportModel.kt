package org.philblandford.ui.imports.model

import org.philblandford.ui.base.viewmodel.VMModel

data class ImportModel(val name:String, val progress:Float = 1f) : VMModel()
