package core.area

import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eav
import com.philblandford.kscore.engine.types.paramMapOf
import assertEqual
import com.philblandford.kscore.engine.core.area.Area
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.crotchet
import org.junit.Test

class AreaTest {
  @Test
  fun testAddArea() {
    var parent = Area(20, 20)
    val child = Area(10, 10)
    parent = parent.addArea(child, Coord(0, 0))
    assertEqual(10, parent.childMap.values.first().width)
    assertEqual(10, parent.childMap.values.first().height)
  }

  @Test
  fun testAddAreaIncreasesSize() {
    var parent = Area()
    val child = Area(10, 10)
    parent = parent.addArea(child, Coord(0, 0))
    assertEqual(10, parent.height)
    assertEqual(10, parent.width)
  }

  @Test
  fun testAddAreaOffsetIncreasesSize() {
    var parent = Area()
    val child = Area(10, 10)
    parent = parent.addArea(child, Coord(10, 10))
    assertEqual(20, parent.height)
    assertEqual(20, parent.width)
  }

  @Test
  fun testAddAbove() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addAbove(child)
    assertEqual(20, parent.height)
    assertEqual(10, parent.width)
    assertEqual(10, parent.yMargin)
  }

  @Test
  fun testAddAboveTwice() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addAbove(child).addAbove(child)
    assertEqual(30, parent.height)
    assertEqual(10, parent.width)
    assertEqual(20, parent.yMargin)
  }

  @Test
  fun testAddAboveShiftDown() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    val child2 = Area(10,10)
    parent = parent.addArea(child2)
    parent = parent.addAbove(child, shiftDown = true)
    assertEqual(20, parent.height)
    assertEqual(10, parent.width)
    assertEqual(0, parent.yMargin)
  }

  @Test
  fun testAddAboveShiftDownXVal() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    val child2 = Area(10,10)
    parent = parent.addArea(child2)
    parent = parent.addAbove(child, x = 5, shiftDown = true)
    assertEqual(20, parent.height)
    assertEqual(15, parent.width)
    assertEqual(0, parent.yMargin)
  }

  @Test
  fun testAddAboveShiftDownGap() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    val child2 = Area(10,10)
    parent = parent.addArea(child2)
    parent = parent.addAbove(child, gap = 5, shiftDown = true)
    assertEqual(25, parent.height)
  }


  @Test
  fun testAddBelow() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addBelow(child)
    assertEqual(20, parent.height)
    assertEqual(10, parent.width)
  }

  @Test
  fun testAddRight() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addRight(child)
    assertEqual(10, parent.height)
    assertEqual(20, parent.width)
  }

  @Test
  fun testAddLeft() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addLeft(child)
    assertEqual(10, parent.height)
    assertEqual(20, parent.width)
    assertEqual(10, parent.xMargin)
  }

  @Test
  fun testAddLeftTwice() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addLeft(child).addLeft(child)
    assertEqual(10, parent.height)
    assertEqual(30, parent.width)
    assertEqual(20, parent.xMargin)
  }

  @Test
  fun testAddBelowYMargin() {
    var parent = Area(10, 10)
    val child1 = Area(10, 10)
    parent = parent.addAbove(child1)
    parent = parent.addBelow(child1)
    assertEqual(30, parent.height)
    assertEqual(10, parent.width)
    assertEqual(10, parent.yMargin)
  }

  @Test
  fun testAddBelowChildHasYMargin() {
    var parent = Area(10, 10)
    val child1 = Area(10, 20, yMargin = 10)
    parent = parent.addBelow(child1, ignoreMargin = true)
    assertEqual(30, parent.height)
    assertEqual(10, parent.width)
    assertEqual(0, parent.yMargin)
  }

  @Test
  fun testAddBelowChildHasYMarginNotIgnored() {
    var parent = Area(10, 10)
    val child1 = Area(10, 20, yMargin = 10)
    parent = parent.addBelow(child1, ignoreMargin = false)
    assertEqual(20, parent.height)
    assertEqual(10, parent.width)
    assertEqual(0, parent.yMargin)
  }


  @Test
  fun testAddBelowWithGap() {
    var parent = Area(10, 10)
    val child = Area(10, 10)
    parent = parent.addBelow(child, 20)
    assertEqual(40, parent.height)
    assertEqual(10, parent.width)
  }

  @Test
  fun testFindByTag() {
    var parent = Area(20, 20)
    val child = Area(10, 10, tag = "Hello")
    parent = parent.addArea(child, Coord(0, 0))
    val found = parent.findByTag("Hello")
    assertEqual(1, found.size)
  }

  @Test
  fun testGetArea() {
    var parent = Area(20, 20)
    val child = Area(10, 10, event = dslChord(crotchet()))
    parent = parent.addArea(child, Coord(0, 0), eav(1))
    parent.getArea(EventType.DURATION, eav(1))!!
  }


  @Test
  fun testAddAreaSameKey() {
    var parent = Area(20, 20)
    val child = Area(10, 10)
    val child2 = Area(10, 10)
    parent = parent.addArea(child).addArea(child2)
    assertEqual(2, parent.childMap.size)
  }

  @Test
  fun testGetAreaSub() {
    var parent = Area(20, 20)
    val child = Area(10, 10)
    val grandChild = Area(10, 10, event = dslChord(crotchet()))
    parent = parent.addArea(child.addArea(grandChild, Coord(0, 0), eav(1)))
    parent.getArea(EventType.DURATION, eav(1))!!

  }

  @Test
  fun testTransformEventAddress() {
    var parent = Area(20, 20)
    val child = Area(10, 10, event = Event(EventType.CLEF, paramMapOf()))
    val grandChild = Area(10, 10, event = dslChord(crotchet()))
    parent = parent.addArea(
      child.addArea(grandChild, Coord(0, 0), eav(1)),
      Coord(0, 0), eav(1)
    )
    parent = parent.transformEventAddress { ea, event -> ea.copy(barNum = 2) }
    parent.getArea(EventType.DURATION, eav(2))!!
    parent.getArea(EventType.CLEF, eav(2))!!

  }

  @Test
  fun testAddAreaRequestedY() {
    var parent = Area(20, 20)
    val child = Area(10, 10, requestedY = 5)
    parent = parent.addArea(child, Coord(0, 0))
    assertEqual(5, parent.yMargin)
    assertEqual(-5, parent.childMap.toList().first().first.coord.y)
  }

  @Test
  fun testAddAreaNegativeY() {
    var parent = Area(20, 20)
    val child = Area(10, 10)
    parent = parent.addArea(child, Coord(0, -5))
    assertEqual(5, parent.yMargin)
    assertEqual(-5, parent.childMap.toList().first().first.coord.y)
  }

  @Test
  fun testGetTopForRange() {
    var parent = Area(50, 20)
    val child = Area(20, 20, drawable = TestDrawable(20,20))
    parent = parent.addAbove(child)
    assertEqual(-20, parent.getTopForRange(5,10))
  }

  @Test
  fun testGetTopForRangeOutside() {
    var parent = Area(50, 20)
    val child = Area(20, 20)
    parent = parent.addAbove(child)
    assertEqual(0, parent.getTopForRange(30,40))
  }

  @Test
  fun testGetTopForRangeSubChild() {
    var parent = Area(50, 20)
    val child = Area(20, 20).addAbove(Area(10,10, drawable = TestDrawable(10,10)))
    parent = parent.addArea(child)
    assertEqual(-10, parent.getTopForRange(5,10))
  }

  @Test
  fun testGetTopForRangeSubChildOffset() {
    var parent = Area(50, 20)
    val child = Area(20, 20).addAbove(Area(10,10, drawable = TestDrawable(10,10)))
    parent = parent.addArea(child, Coord(20,0))
    assertEqual(-10, parent.getTopForRange(20,25))
  }

  @Test
  fun testGetTopForRangeOutsideSubChild() {
    var parent = Area(50, 20)
    val child = Area(20, 20).addAbove(Area(10,10, drawable = TestDrawable(10,10)))
    parent = parent.addArea(child)
    assertEqual(0, parent.getTopForRange(15,20))
  }

  @Test
  fun testExtendRight() {
    val area = Area(width = 20, height = 50)
    assertEqual(40, area.extendRight(20).width)
  }

  @Test
  fun testExtendLeft() {
    val area = Area(width = 20, height = 50)
    assertEqual(40, area.extendLeft(20).width)
  }

  @Test
  fun testExtendLeftChildMoves() {
    var area = Area(width = 20, height = 50).addArea(Area(10,10))
    area = area.extendLeft(20)
    val childX = area.childMap.toList().first().first.coord.x
    assertEqual(20, childX)
  }


  @Test
  fun testRemoveMarginY() {
    val area = Area(width = 20, height = 50, yMargin = 20)
    val removed = area.removeYMargin()
    assertEqual(0, removed.yMargin)
  }

  @Test
  fun testRemoveMarginYChild() {
    var area = Area(width = 20, height = 50)
    area = area.addArea(Area(width = 10, height = 10), Coord(0, -10))
    val removed = area.removeYMargin()
    assertEqual(0, removed.yMargin)
    assertEqual(0, removed.childMap.toList().first().first.coord.y)
  }

  @Test
  fun testRemoveMarginYChildHeightCorrect() {
    var area = Area(width = 20, height = 50)
    area = area.addArea(Area(width = 10, height = 10), Coord(0, -10))
    val removed = area.removeYMargin()
    assertEqual(60, removed.height)
  }

  @Test
  fun testRemoveMarginYChildStraddleMargin() {
    var area = Area(width = 20, height = 50)
    area = area.addArea(Area(width = 10, height = 40), Coord(0, -10))
    val removed = area.removeYMargin()
    assertEqual(60, removed.height)
  }


  private class TestDrawable(override val width:Int, override val height:Int,
                             override val effectiveHeight: Int = height,
                             override val trim:Int = 0,
                             override val export:Boolean = true) : KDrawable {


    override fun draw(x: Int, y: Int, export: Boolean, vararg args: Any) {

    }
  }
}