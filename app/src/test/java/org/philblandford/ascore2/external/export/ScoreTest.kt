package org.philblandford.ascore2.external.export

import com.philblandford.kscore.api.DrawableGetter
import com.philblandford.kscore.engine.beam.BeamMap
import com.philblandford.kscore.engine.core.area.KDrawable
import com.philblandford.kscore.engine.core.area.factory.BeamArgs
import com.philblandford.kscore.engine.core.area.factory.DiagonalArgs
import com.philblandford.kscore.engine.core.area.factory.DotArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableArgs
import com.philblandford.kscore.engine.core.area.factory.DrawableFactory
import com.philblandford.kscore.engine.core.area.factory.ImageArgs
import com.philblandford.kscore.engine.core.area.factory.LineArgs
import com.philblandford.kscore.engine.core.area.factory.RectArgs
import com.philblandford.kscore.engine.core.area.factory.SlurArgs
import com.philblandford.kscore.engine.core.area.factory.TextArgs
import com.philblandford.kscore.engine.core.representation.BLOCK_HEIGHT
import com.philblandford.kscore.engine.core.representation.LINE_THICKNESS
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.core.score.ScoreContainer
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.implementations.BaseInstrumentGetter
import com.philblandford.kscore.saveload.readdMeta
import com.philblandford.kscore.option.getOption
import com.philblandford.kscore.engine.core.representation.SHARP_WIDTH
import org.junit.Before
import kotlin.math.abs

open class ScoreTest {

    protected val instrumentGetter = BaseInstrumentGetter(InstrumentData, null).apply { refresh() }
    protected val drawableGetter = TestDrawableGetter
    protected val drawableFactory = DrawableFactory(drawableGetter)

    protected val sc = ScoreContainer(drawableFactory).apply {
        synchronous = true
        setExceptionHandler { throw (it) }
    }

    @Before
    open fun setup() {
        sc.setNewScoreNoRepresentation(Score.create(instrumentGetter, 32))
    }

    fun replaceScore(score: Score) {
        sc.setNewScoreNoRepresentation(score)
    }

    fun SCD(
        timeSignature: TimeSignature = TimeSignature(4, 4),
        ks: Int = 0,
        instruments: Iterable<String> = listOf("Violin"),
        bars: Int = 32,
        upbeat: TimeSignature? = null
    ) {
        val score = Score.create(
            instrumentGetter,
            bars, timeSignature, ks, instruments = instruments,
            upbeat = upbeat
        ).readdMeta()
        sc.setNewScoreNoRepresentation(score)
    }

    fun SCDG() {
        SCD(instruments = listOf("Piano"))
    }

    fun SCDT() {
        SCD(instruments = listOf("Trumpet"))
    }

    fun RCD(
        timeSignature: TimeSignature = TimeSignature(4, 4),
        ks: Int = 0,
        instruments: Iterable<String> = listOf("Violin"),
        bars: Int = 32,
        upbeat: TimeSignature? = null
    ) {
        val score = Score.create(
            instrumentGetter,
            bars, timeSignature, ks, instruments = instruments,
            upbeat = upbeat
        )
        sc.setNewScore(score)
    }

    fun SMV(
        midiVal: Int = 72, duration: Duration = crotchet(),
        accidental: Accidental = Accidental.SHARP, eventAddress: EventAddress = eav(1),
        extraParams: ParamMap = paramMapOf(), endAddress: EventAddress? = null
    ) {
        sc.addEvent(
            Event(
                EventType.NOTE, paramMapOf(
                    EventParam.MIDIVAL to midiVal,
                    EventParam.DURATION to duration,
                    EventParam.ACCIDENTAL to accidental
                ).plus(extraParams)
            ), eventAddress, endAddress
        )
    }


    fun SAE(
        eventType: EventType, eventAddress: EventAddress = ea(1), params: ParamMap = paramMapOf(),
        endAddress: EventAddress? = null
    ) {
        sc.addEvent(Event(eventType, params), eventAddress, endAddress)
    }

    fun SAE(event: Event, eventAddress: EventAddress = ea(1)) {
        sc.addEvent(event, eventAddress)
    }


    fun SDE(
        eventType: EventType, eventAddress: EventAddress = ea(1),
        endAddress: EventAddress? = null
    ) {
        sc.deleteEvent(eventType, eventAddress, endAddress)
    }

    fun SDR(start: EventAddress, end: EventAddress) {
        sc.deleteRange(start, end)
    }

    fun SSP(
        eventType: EventType,
        eventParam: EventParam,
        value: Any?,
        eventAddress: EventAddress = eZero(),
        endAddress: EventAddress? = null
    ) {
        sc.setParam(eventType, eventParam, value, eventAddress, endAddress)
    }


    fun <T> SSO(option: EventParam, value: T) {
        sc.setOption(option, value)
    }

    fun <T : Any> SVO(option: EventParam, expected: T, cludge: T.() -> Any = { this }) {
        val opt = getOption<T>(option, SCORE())
        assertEqual(expected.cludge(), opt.cludge())
    }

    fun <T> SGO(option: EventParam): T? {
        return getOption(option, SCORE())
    }

    fun SVE(
        eventType: EventType,
        eventAddress: EventAddress = ea(1)
    ) {
        assert(SCORE().getEvent(eventType, eventAddress) != null)
    }

    fun SVA(
        eventType: EventType,
        eventAddress: EventAddress = ea(1)
    ) {
        assert(SCORE().getEventAt(eventType, eventAddress) != null)
    }


    fun SVNE(
        eventType: EventType,
        eventAddress: EventAddress = ea(1)
    ) {
        assert(SCORE().getEvent(eventType, eventAddress) == null)
    }

    fun <T> SVP(
        eventType: EventType,
        eventParam: EventParam,
        value: T?,
        eventAddress: EventAddress = ea(1)
    ) {
        assertEqual(value, SCORE().getParam<T>(eventType, eventParam, eventAddress))
    }

    fun <T> SVPA(
        eventType: EventType,
        eventParam: EventParam,
        value: T?,
        eventAddress: EventAddress = ea(1)
    ) {
        assertEqual(value, SCORE().getParamAt<T>(eventType, eventParam, eventAddress))
    }

    fun SVNP(
        eventType: EventType,
        eventParam: EventParam,
        eventAddress: EventAddress = ea(1)
    ) {
        assert(SCORE().getParam<Any>(eventType, eventParam, eventAddress) == null)
    }

    fun SVNPA(
        eventType: EventType,
        eventParam: EventParam,
        eventAddress: EventAddress = ea(1)
    ) {
        assert(SCORE().getParamAt<Any>(eventType, eventParam, eventAddress) == null)
    }

    fun SVB(
        eventType: EventType,
        eventParam: EventParam,
        expected: Boolean,
        eventAddress: EventAddress = ea(1)
    ) {
        assertEqual(expected, SCORE().getParam<Boolean>(eventType, eventParam, eventAddress))
    }


    fun SVVM(string: String, eventAddress: EventAddress) {
        assertEqual(string, SCORE().getVoiceMap(eventAddress)?.eventString())
    }

    fun EG(): Score = SCORE()

    fun getEvent(eventType: EventType, eventAddress: EventAddress) =
        SCORE().getEvent(eventType, eventAddress)

    fun setNewScore(score: Score) {
        sc.setNewScore(score)
    }

    fun setMarker(eventAddress: EventAddress) {
        sc.setParam(EventType.UISTATE, EventParam.MARKER_POSITION, eventAddress, eZero())
    }

    fun getMarker(): EventAddress? {
        return sc.getParam<EventAddress>(
            EventType.UISTATE,
            EventParam.MARKER_POSITION,
            eZero(),
            null
        ) as EventAddress
    }

    fun BeamMap.getBeamStrings(): List<String> {
        return values.map {
            it.members.map {
                if (it.duration.numerator == 1) {
                    it.duration.denominator
                } else {
                    "${it.duration.numerator}/${it.duration.denominator}"
                }
            }.joinToString(":")
        }
    }

    protected fun SCORE() = sc.currentScoreState.value.score!!



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
}