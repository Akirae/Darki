package com.akirae.cryptophoto

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun mask(){
         val significantBitsCount = 4
         val maske = (255 shr significantBitsCount) shl significantBitsCount
         val maskd = (255 shr (8 - significantBitsCount)).shl(4).inv()
        assertEquals(maske, 240)
        assertEquals(maskd, 15)

    }
}