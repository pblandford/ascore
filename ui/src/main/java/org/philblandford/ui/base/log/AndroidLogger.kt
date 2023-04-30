package org.philblandford.ui.base.log

import android.util.Log
import com.philblandford.kscore.log.KSLogger
import com.philblandford.kscore.log.LogLevel
import timber.log.Timber

class AndroidLogger : KSLogger {
  override fun log(logLevel: LogLevel, msg: String, exception: Throwable?) {
    try {
    when (logLevel) {
      LogLevel.VERBOSE -> Timber.v(msg, exception)
      LogLevel.DEBUG -> Timber.d(msg, exception)
      LogLevel.WARNING -> Timber.w(msg, exception)
      LogLevel.ERROR -> Timber.e(msg, exception)
      LogLevel.FATAL -> Timber.e(msg, exception)
      LogLevel.TRACKING -> Timber.e(msg, exception)
    }
      } catch (e:Exception) {
        Log.e("TIMBER", "Timber!", e)
      }
  }
}