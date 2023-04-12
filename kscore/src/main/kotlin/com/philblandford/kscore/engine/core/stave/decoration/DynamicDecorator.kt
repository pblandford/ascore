package com.philblandford.kscore.engine.core.stave.decoration

import com.philblandford.kscore.engine.core.representation.DYNAMIC_HEIGHT
import com.philblandford.kscore.engine.types.DynamicType
import com.philblandford.kscore.engine.types.DynamicType.*
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventParam

object DynamicDecorator : UpDownDecorator {

  override fun getAreaKey(event: Event): String? {
    return event.getParam<DynamicType>(EventParam.TYPE)?.let { keys[it] }
  }

  override fun getAreaHeight(event:Event) = event.subType?.let {
    getHeight(it as DynamicType)
  } ?: DYNAMIC_HEIGHT

  override fun getAreaTag() = "Dynamic"
}

private fun getHeight(dynamicType: DynamicType):Int {
  return if (dynamicType.toString().contains("FORT")) {
    DYNAMIC_HEIGHT
  } else {
    (DYNAMIC_HEIGHT * 0.75).toInt()
  }
}

private val keys = mapOf(
  MOLTO_PIANISSIMO to "dynamic_molto_pianissimo",
  PIANISSIMO to "dynamic_pianissimo",
  PIANO to "dynamic_piano",
  MEZZO_PIANO to "dynamic_mezzo_piano",
  MEZZO_FORTE to "dynamic_mezzo_forte",
  FORTE to "dynamic_forte",
  FORTISSIMO to "dynamic_fortissimo",
  MOLTO_FORTISSIMO to "dynamic_molto_fortissimo",
  SFORZANDO to "dynamic_sforzando",
  SFORZANDISSMO to "dynamic_sforzandissimo",
  FORTE_PIANO to "dynamic_forte_piano",
  SFORZANDO_PIANO to "dynamic_sforzando_piano"
)
