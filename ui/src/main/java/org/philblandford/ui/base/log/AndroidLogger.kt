package org.philblandford.ui.base.log

import android.util.Log
import com.philblandford.kscore.log.KSLogger
import com.philblandford.kscore.log.LogLevel

class AndroidLogger : KSLogger {
  override fun log(logLevel: LogLevel, msg: String, exception: Throwable?) {
    Log.e("KSCORE", msg, exception)
  }
}