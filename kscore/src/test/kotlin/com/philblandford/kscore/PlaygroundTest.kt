package com.philblandford.kscore

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PlaygroundTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFlatmap() {
        runBlocking {
            val flow = flowOf(1, 2, 3).onEach { delay(1000) }
            val flow2 = flowOf(4, 5, 6).onEach { delay(1000) }
            var flow2cnt = 10
            flow.flatMapLatest { x ->
                println(x)
                val marker = flow2cnt
                flow2cnt += 10
                flow2.map { it + marker }
            }.collect {
                println(it)
            }
        }
    }
}