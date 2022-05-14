package org.philblandford.ascore2.stubs

import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.DrawableArgs

class StubDrawableGetter : DrawableGetter {
  override fun getDrawable(drawableArgs: DrawableArgs): KDrawable? {
    return null
  }

  override fun prepare(vararg args: Any) {

  }
}