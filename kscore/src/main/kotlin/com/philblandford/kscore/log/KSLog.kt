package com.philblandford.kscore.log

/**
 * Created by phil on 04/12/18.
 */

enum class LogLevel {
  VERBOSE, DEBUG, WARNING, ERROR, FATAL, TRACKING
}

private var logger: KSLogger? = null

interface KSLogger {
  fun log(logLevel: LogLevel, msg: String, exception: Throwable? = null)
}

fun ksLog(logLevel: LogLevel, msg:String, exception: Throwable? = null) {
  logger?.log(logLevel, msg, exception)
}

fun registerLogger(ksLogger: KSLogger) {
  logger = ksLogger
}

fun ksLogv(msg:String) {
  ksLog(LogLevel.VERBOSE, msg)
}

fun ksLogd(msg:String) {
  ksLog(LogLevel.DEBUG, msg)
}

fun ksLoge(msg:String, exception: Throwable? = null) {
  ksLog(LogLevel.ERROR, msg, exception)
}

fun ksLogt(msg:String = "", exception: Throwable? = null) {
  ksLog(LogLevel.TRACKING, msg, exception)
}