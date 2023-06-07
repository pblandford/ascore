package com.philblandford.kscore.engine.core.representation

import TestInstrumentGetter
import assertEqual
import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.area.*
import com.philblandford.kscore.engine.core.area.factory.*
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import org.junit.After
import java.lang.Exception
import kotlin.math.abs

open class RepTest : ScoreTest() {

  @Override
  override fun setup() {
    sc.setNewScore(Score.create(TestInstrumentGetter(), 32))
  }

  @After
  fun tearDown() {
  }


  class TestDrawable(
    override val width: Int = 0,
    override val height: Int = 0,
    override val effectiveHeight: Int = height,
    val tag: String = "",
    override val trim: Int = 0,
    override val export: Boolean = true,
    val text: String? = null,
    val imageKey: String? = null
  ) : KDrawable {


    override fun draw(x: Int, y: Int, export: Boolean, vararg args: Any) {
    }
  }

  object TestDrawableGetter : TestDrawableGetterIf
  interface TestDrawableGetterIf : DrawableGetter {
    override fun getDrawable(drawableArgs: DrawableArgs): KDrawable? {

      return when (drawableArgs) {
        is TextArgs -> TestDrawable(
          drawableArgs.text.length * drawableArgs.size / 2,
          20,
          text = drawableArgs.text
        )
        is LineArgs -> if (drawableArgs.horizontal) {
          TestDrawable(drawableArgs.length, LINE_THICKNESS)
        } else {
          TestDrawable(LINE_THICKNESS, drawableArgs.length)
        }
        is ImageArgs -> {
          val width = getWidth(drawableArgs)
          TestDrawable(width, drawableArgs.height, tag = drawableArgs.name)
        }
        is SlurArgs -> TestDrawable(
          drawableArgs.end.x - drawableArgs.start.x,
          abs(drawableArgs.end.y - drawableArgs.start.y) + BLOCK_HEIGHT * 2
        )
        is RectArgs -> TestDrawable(drawableArgs.width, drawableArgs.height)
        is DiagonalArgs -> TestDrawable(drawableArgs.width, drawableArgs.height)
        is DotArgs -> TestDrawable(drawableArgs.width, drawableArgs.height)
        is BeamArgs -> TestDrawable(drawableArgs.width, drawableArgs.height)
        else -> TestDrawable(0, 0)
      }
    }

    private fun getWidth(imageArgs: ImageArgs): Int {
      return if (imageArgs.name.contains("accidental")) SHARP_WIDTH else
        if (imageArgs.width == INT_WILD) imageArgs.height else imageArgs.width
    }

    override fun prepare(vararg args: Any) {

    }
  }

  fun RVA(tag: String, eventAddress: EventAddress) {
    getArea(tag, eventAddress)?.eventAddress?.let { ea ->
      assertEqual(eventAddress, ea)
    } ?: run {
      throw Exception("Area $tag not found at $eventAddress")
    }
  }

  fun RVNA(tag: String, eventAddress: EventAddress) {
    assert(getArea(tag, eventAddress)?.eventAddress == null)
  }

  fun RCoord(tag: String, eventAddress: EventAddress): Coord? {
    return getArea(tag, eventAddress)?.coord
  }

  fun isAbove(tag1: String, ea1: EventAddress, tag2: String, ea2: EventAddress): Boolean? {
    return getArea(tag1, ea1)?.let { first ->
      getArea(tag2, ea2)?.let { second ->
        first.coord.y + first.area.height < second.coord.y
      }
    }
  }


  fun isLeft(tag1: String, ea1: EventAddress, tag2: String, ea2: EventAddress): Boolean? {
    return getArea(tag1, ea1)?.let { first ->
      getArea(tag2, ea2)?.let { second ->
        first.coord.x + first.area.width <= second.coord.x
      }
    }
  }

  fun isInsideY(yPos: Int, tag: String, ea: EventAddress): Boolean? {
    return getArea(tag, ea)?.let { area ->
      area.coord.y <= yPos && area.coord.y + area.area.height >= yPos
    }
  }

  data class AreaReturn(val eventAddress: EventAddress, val coord: Coord, val area: Area)

  fun getArea(tag: String, ea: EventAddress): AreaReturn? {
    return REP().pages.mapNotNull { pageArea ->
      pageArea.base.getArea(ea) {
        it.tag == tag
      }
    }.firstOrNull()?.let {
      AreaReturn(it.first.eventAddress, it.first.coord, it.second)
    }
  }


  fun getAreas(tag: String): Map<AreaMapKey, Area> {
    return REP().pages.flatMap {
      it.base.findByTag(tag).toList()
    }.toMap()
  }

  fun getStaveBar(stave: Int): Int {
    return getAreas("Stave").toList()
      .sortedBy { it.first.eventAddress }[stave].first.eventAddress.barNum

  }

  protected fun REP() = sc.currentScoreState.value.representation!!
}