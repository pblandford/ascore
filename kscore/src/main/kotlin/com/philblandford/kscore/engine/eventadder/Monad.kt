package com.philblandford.kscore.engine.eventadder

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType

sealed class Failure(open val message: String, open val exception: Exception? = null)
open class Error(override val message: String, override val exception: Exception = Exception()) :
    Failure(message, exception)

data class NoDestinationFailure(val eventType: EventType) :
    Failure("No destination found for $eventType")

data class HarmlessFailure(override val message: String) : Failure(message)

data class NotFound(override val message: String = "Not found") : Error(message)
data class ParamsMissing(
    val keys: List<EventParam>,
    override val message: String = "Require params: $keys"
) : Error(message)

fun asError(message: String): Left<Failure> {
    return Left(Error(message))
}

fun <R> asWarning(message: String, restore: R): Warning<Failure, R> {
    return Warning(message, restore)
}

sealed class ASResult<out L : Failure, out R>(
    open val messages: List<Exception> = listOf()
) {
    fun copyMessages(messages: List<Exception> = listOf()): ASResult<L, R> {
        return when (this) {
            is Warning -> copy(messages = messages)
            is Left -> copy(messages = messages)
            is AbortNoError -> copy(messages = messages)
            is Right -> copy(messages = messages)
        }
    }
}

data class Left<out L : Failure>(
    val l: L,
    override val messages: List<Exception> = listOf()
) : ASResult<L, Nothing>(messages)


data class Warning<out L : Failure, out R>(
    val l: L,
    val r: R,
    override val messages: List<Exception> = listOf()
) : ASResult<L, R>(messages) {
    constructor(message: String = "", r: R) : this(HarmlessFailure(message) as L, r)
}

data class AbortNoError<out L : Failure>(
    val l: L,
    override val messages: List<Exception> = listOf()
) : ASResult<L, Nothing>(messages) {
    constructor(message: String = "") : this(HarmlessFailure(message) as L, listOf())
}

data class Right<out R>(val r: R, override val messages: List<Exception> = listOf()) :
    ASResult<Nothing, R>(messages)


infix fun <L : Failure, R1, R2> ASResult<L, R1>.then(f: (R1) -> ASResult<L, R2>): ASResult<L, R2> {
    return when (this) {
        is Left -> this
        is AbortNoError -> this
        is Warning -> {
            val exception = Exception(this.l.message, this.l.exception)
            val res = f(this.r)
            res.copyMessages(messages.plus(exception))
        }

        is Right -> {
            val res = f(this.r)
            res.copyMessages(messages)
        }
    }
}

fun <L : Failure, R> ASResult<L, R>.ignoreFailure(originalThing: R): ASResult<L, R> =
    when (this) {
        is Left ->
            Warning(
                l,
                originalThing,
                messages.plus(Exception("Ignoring failure ${l.message}", l.exception))
            )

        else -> this
    }

fun <L : Failure, R> ASResult<L, R>.failureIsNoop(
    originalThing: R,
    ifOkDo: (R) -> ASResult<L, R>
): ASResult<L, R> =
    when (this) {
        is Left ->
            Warning(
                l,
                originalThing,
                messages.plus(Exception("Ignoring failure ${l.message}", l.exception))
            )

        is Warning -> ifOkDo(this.r)
        is AbortNoError -> this
        is Right -> ifOkDo(this.r)
    }

fun <L : Failure, R> ASResult<L, R>.failureIsNoop(originalThing: R): ASResult<L, R> =
    when (this) {
        is Left ->
            Warning(
                l,
                originalThing,
                messages.plus(Exception("Ignoring failure ${l.message}", l.exception))
            )

        else -> this
    }


inline fun <L : Failure, R> ASResult<L, R>.otherwise(a: () -> ASResult<L, R>): ASResult<L, R> =
    when (this) {
        is Left, is AbortNoError -> a()
        else -> this
    }

typealias AnyResult<T> = ASResult<Failure, T>

fun <A, B> Iterable<A>.mapOrFail(f: (A) -> AnyResult<B>): AnyResult<List<B>> {
    return this.fold(Right(listOf<B>()) as AnyResult<List<B>>) { listRes, elem ->
        listRes.then { list ->
            f(elem).then { Right(list.plus(it)) }
        }
    }
}

fun <R, I> R.fold(items: Iterable<I>, action: R.(I) -> AnyResult<R>): AnyResult<R> {
    return items.fold(Right(this) as AnyResult<R>) { r, i ->
        r.then {
            it.action(i)
        }
    }
}


fun <R> AnyResult<R>.rightOrThrow(): R {
    return when (this) {
        is Left -> throw Exception(this.l.message, this.l.exception)
        is AbortNoError -> throw Exception("Abort no error $this", this.l.exception)
        is Warning -> this.r
        is Right -> this.r
    }
}

fun <R> AnyResult<R>.rightOrNull(): R? {
    return when (this) {
        is Left -> null
        is AbortNoError -> null
        is Warning -> this.r
        is Right -> this.r
    }
}

fun <R> R?.notNull(): AnyResult<R> {
    return this?.let { Right(it) } ?: asError("Got null")
}

fun <R1, R2> R1?.ifNullError(
    message: String = "Got null",
    f: (R1) -> AnyResult<R2>
): AnyResult<R2> {
    return this?.let { Right(it).then(f) } ?:
        asError(message)
}


fun <R1> R1?.ifNullFail(
    message: String = "Got null"
): AnyResult<R1> {
    return this?.let { Right(it) } ?: asError(message)
}


fun <R1, R2> R1?.ifNullWarn(
    restore: R2,
    message: String = "Got null",
    f: (R1) -> AnyResult<R2>
): AnyResult<R2> {
    return this?.let { Right(it).then(f) } ?: asWarning(message, restore)
}


fun <R1, R2> R1?.ifNullRestore(restore: R2, f: (R1) -> AnyResult<R2>): AnyResult<R2> {
    return this?.let { Right(it).then(f) } ?: Right(restore)
}

fun <R> R.ok() = Right(this)