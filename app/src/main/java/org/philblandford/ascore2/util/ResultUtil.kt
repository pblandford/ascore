package org.philblandford.ascore2.util

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.score.Score

fun <T>KScore.action(func:Score.()->T):Result<T> {
  return getScore()?.let { score ->
    try {
      score.func().ok()
    } catch (e:Exception) {
      Result.failure(e)
    }
  } ?: failure("Score not found")
}

suspend fun <T, U> Result<T>.andThen(func: suspend (T) -> Result<U>): Result<U> {

  return this.exceptionOrNull()?.let { exception ->
    Result.failure(exception)
  } ?: run {
    getOrNull()?.let {
      func(it)
    } ?: run {
      Result.failure(Exception("Value is null"))
    }
  }
}

suspend fun <T, U> Result<T>.andThenNullable(func: suspend (T?) -> Result<U>): Result<U> {

  return this.exceptionOrNull()?.let { exception ->
    Result.failure(exception)
  } ?: run {
    return func(this.getOrNull())
  }
}

suspend fun <T> Result<T>.finally(func: suspend Result<T>.() -> Unit): Result<T> {
  this.func()
  return this
}

fun <T> T.ok(): Result<T> {
  return Result.success(this)
}

fun <T> Result<T>.orElse(): T {
  return getOrNull()!!
}

fun <T> failure(string: String): Result<T> = Result.failure(Exception(string))

fun <T>tryResult(func:()->Result<T>):Result<T> {
  return try {
    func()
  } catch (e:Exception) {
    Result.failure(e)
  }
}
