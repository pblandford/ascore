package com.philblandford.kscore.engine.util

import kotlinx.coroutines.*

suspend fun <A, B> Iterable<A>.pMap(f: suspend (A) -> B): List<B> = coroutineScope {
  map { async { f(it) } }.awaitAll()
}

suspend fun <T> Iterable<T>.pForEach(action: suspend (T) -> Unit) = coroutineScope {
  forEach { withContext(Dispatchers.Default) { action(it) } }
}

suspend fun <K, V> Map<out K, V>.pForEach(action: (Map.Entry<K, V>) -> Unit) = coroutineScope {
  forEach { withContext(Dispatchers.Default) { action(it) } }
}

fun <T, R> Iterable<T>.pFlatMap(transform: suspend (T) -> Iterable<R>) = runBlocking {
  map { set ->
    async {
      transform(set) }
  }.flatMap { deferred ->
    deferred.await()
  }
}
