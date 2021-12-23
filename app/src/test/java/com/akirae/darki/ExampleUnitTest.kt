package com.akirae.darki

import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun encrypt() {
        val significantBitsCount = 3
        val mask = (255 shr significantBitsCount) shl significantBitsCount
        val coverR = 167
        val coverG = 93
        val coverB = 27

        val secretR = 67
        val secretG = 200
        val secretB = 105

        val resR = (coverR and mask) or secretR.and(255).shr(8 - significantBitsCount)
        val resG = (coverG and mask) or secretG.shr(8 - significantBitsCount)
        val resB = (coverB and mask) or secretB.shr(8 - significantBitsCount)

        val co = coverR.toString(2)
        val s = secretR.and(255).toString(2)
        val a = coverR.and(mask).toString(2)
        val b = secretR.shr(8 - significantBitsCount).toString(2)
        val c = ((coverR and mask) or secretR.shr(8 - significantBitsCount)).toString(2)
        val d = a + b + c + co + s
        assertEquals(resR, 162)
        assertEquals(resG, 94)
        assertEquals(resB, 27)
    }

    @Test
    fun decrypt() {
        val significantBitsCount = 3

        val coverR = 162
        val coverG = 94
        val coverB = 27

        val resR: Int = coverR.shl(8 - significantBitsCount) and 255
        val resG: Int = coverG.shl(8 - significantBitsCount) and 255
        val resB: Int = coverB.shl(8 - significantBitsCount) and 255

        assertEquals(resR, 64)
        assertEquals(resG, 192)
        assertEquals(resB, 96)
    }

    @Test
    fun parallel() {
        val parMapTime = measureTimeMillis {
            runBlocking (Dispatchers.IO) {
                (1..600_000)
                    .toList()
                    .mapParallel { it * 3 }
            }
        }
        println(parMapTime)
    }


    suspend fun <T, R> Iterable<T>.mapParallel(transform: (T) -> R): List<R> = coroutineScope {
        map { async (Dispatchers.Default)  { transform(it) } }.awaitAll()
    }
}