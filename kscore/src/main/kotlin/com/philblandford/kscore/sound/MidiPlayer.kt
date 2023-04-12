package com.philblandford.kscore.sound

import com.philblandford.kscore.api.SoundManager
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.EventAddress
import com.philblandford.kscore.engine.types.ScoreQuery
import com.philblandford.kscore.engine.types.eas
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.log.ksLogv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PlayState {
    PLAYING, PAUSED, STOPPED
}
private data class PauseState(val currentMs: MS, val midiPlayLookup: MidiPlayLookup)

class MidiPlayer(private val soundManager: SoundManager) {

    private var pauseState: PauseState? = null
    private var midiPlayLookup: MidiPlayLookup? = null
    private var midiScheduler: MidiScheduler? = null
    private val playbackMarker = MutableStateFlow<EventAddress?>(null)
    private val playState = MutableStateFlow(PlayState.STOPPED)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun getPlaybackMarker():StateFlow<EventAddress?> = playbackMarker
    fun getPlayState():StateFlow<PlayState> = playState

    fun play(
        score: ScoreQuery,
        loop: () -> Boolean = { true },
        start: EventAddress? = null, end: EventAddress? = null,
        getLiveVelocityAdjust: (Int) -> Float = { 1f }
    ) {
        val startEnd = interpretStartEnd(start, end, score)
        val realStart = startEnd.first
        val realEnd = startEnd.second

        midiPlayLookup = pauseState?.midiPlayLookup ?: midiPlayLookup(score, soundManager, realStart, realEnd)
        val startMs = pauseState?.currentMs ?: midiPlayLookup?.addressToMs(realStart) ?: 0
        val endMs =
            (midiPlayLookup?.addressToMs(realEnd) ?: midiPlayLookup?.lastMs() ?: 0) + 10

        initPlayback(startMs, endMs, loop, {
            coroutineScope.launch { playbackMarker.emit(it) }
        }, getLiveVelocityAdjust)
    }

    private fun initPlayback(
        startMs: Int, endMs: Int, loop: () -> Boolean,
        callback: (EventAddress) -> Unit = {},
        getLiveVelocityAdjust: (Int) -> Float = { 1f }
    ) {
        stop(false)

        ksLogt("assigning midiScheduler")
        midiScheduler = MidiScheduler()

        midiPlayLookup?.let {
            updatePlayState(PlayState.PLAYING)
            sendProgramChangeEvents(it, startMs)
            midiScheduler?.run({ ms ->

                if (ms == endMs) {
                    if (loop()) {
                        stopAllNotes()
                        midiScheduler?.setPosition(startMs)
                    } else {
                        stop(true)
                    }
                }
                schedulerCallback(ms, callback, getLiveVelocityAdjust)
            }, startMs)
            pauseState = PauseState(0, it)
        }
    }

    private fun updatePlayState(playState: PlayState) {
        coroutineScope.launch {
            this@MidiPlayer.playState.emit(playState)
        }
    }

    private fun stopAllNotes() {
        midiPlayLookup?.getProgramEvents(null)?.groupBy { it.soundFont }?.forEach { (sf, _) ->
            soundManager.stopAllNotes(sf)
        }
    }

    fun refresh(score: ScoreQuery) {
        midiPlayLookup = midiPlayLookup(score, soundManager)
        stopAllNotes()
        midiPlayLookup?.let {
            sendProgramChangeEvents(it, 0)
        }
    }

    fun pause() {
        stopAllNotes()
        pauseState = pauseState?.copy(currentMs = midiScheduler?.getPosition() ?: 0)
        midiScheduler?.pause()
        updatePlayState(PlayState.PAUSED)
    }

    fun stop(callback: Boolean = true) {
        ksLogt("Stopping $callback")
        midiScheduler?.stop()
        midiScheduler = null
        if (callback) {
            coroutineScope.launch { playbackMarker.emit(null) }
        }
        postStop()
    }

    fun isPlaying(): Boolean {
        return midiScheduler != null
    }

    fun isPaused(): Boolean {
        return midiScheduler?.isPaused() == true
    }

    private fun postStop() {
        ksLogv("Stopping all notes")
        updatePlayState(PlayState.STOPPED)
        stopAllNotes()
        pauseState = null
    }

    private fun interpretStartEnd(
        start: EventAddress?, end: EventAddress?,
        scoreQuery: ScoreQuery
    ): Pair<EventAddress, EventAddress> {
        val realStart = start ?: eas(1, dZero(), scoreQuery.getAllStaves(true).first())
        val realEnd = if (end == null || end == start) {
            eas(scoreQuery.numBars + 1, dZero(), scoreQuery.getAllStaves(true).last())
        } else {
            scoreQuery.getEventEnd(end) ?: end
        }
        return Pair(realStart, realEnd)
    }


    private fun sendProgramChangeEvents(midiPlayLookup: MidiPlayLookup, ms: MS) {
        midiPlayLookup.getProgramEvents(ms).forEach { playMidiEvent(it) }
    }

    private fun schedulerCallback(
        ms: Int,
        higherCallBack: (EventAddress) -> Unit,
        velocityAdjust: (Int) -> Float
    ): Boolean {
        if (ms % 100 == 0) {
            ksLogv("MS $ms")
        }
        midiPlayLookup?.let {
            it.getEvents(ms)?.let { events ->
                events.forEach { midiEvent ->
                    ksLogv("playing event $ms $midiEvent")
                    val adjusted = adjustEvent(midiEvent, it, velocityAdjust)
                    playMidiEvent(adjusted)
                }
            }
            it.msToAddress(ms)?.let {
                ksLogv("higher callback")
                higherCallBack(it)
                ksLogv("Done")
            }
            return true
        }
        return false
    }

    private fun adjustEvent(
        midiEvent: MidiEvent,
        midiPlayLookup: MidiPlayLookup,
        velocityAdjust: (Int) -> Float
    ): MidiEvent {
        return when (midiEvent) {
            is NoteOnEvent -> {
                val part = midiPlayLookup.channelToStave(midiEvent.channel)
                midiEvent.copy(velocity = (midiEvent.velocity * velocityAdjust(part.main)).toInt())
            }
            else -> midiEvent
        }
    }

    private fun playMidiEvent(midiEvent: MidiEvent) {
        when (midiEvent) {
            is NoteOnEvent -> soundManager.noteOn(
                midiEvent.midiVal,
                midiEvent.velocity,
                midiEvent.channel
            )
            is NoteOffEvent -> soundManager.noteOff(midiEvent.midiVal, midiEvent.channel)
            is ProgramChangeEvent -> soundManager.programChange(
                midiEvent.program, midiEvent.channel,
                midiEvent.soundFont, midiEvent.bank
            )
            is PedalEvent -> soundManager.pedalEvent(midiEvent.on, midiEvent.channel)
            else -> {}
        }
    }
}
