package com.philblandford.kscore.sound

import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MidiScheduler {

  private var currentMs = 0
  private var timer: Timer? = null
  private var executor: ScheduledThreadPoolExecutor? = null
  private var future: ScheduledFuture<*>? = null
  private var playing:Boolean = false

  fun pause() {
    future?.cancel(true)
    future = null
  }

  fun stop() {
    playing = false
    future?.cancel(true)
    future = null
    currentMs = 0
  }

  fun isPlaying() = playing

  fun isPaused() = playing && future == null

  fun getPosition() = currentMs

  fun setPosition(pos:Int) {
    currentMs = pos
  }

  fun run(callback: (Int) -> Unit, startMs: Int = 0) {
    timer?.cancel()
    currentMs = startMs
    playing = true
    executor = ScheduledThreadPoolExecutor(1)
    future = executor?.scheduleAtFixedRate({
      callback(currentMs++)
    }, 0, 1, TimeUnit.MILLISECONDS)
  }

}