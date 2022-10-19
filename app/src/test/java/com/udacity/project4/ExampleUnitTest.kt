package com.udacity.project4

import android.os.Build
import org.junit.Test

import org.junit.Assert.*
import org.robolectric.annotation.Config

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class ExampleUnitTest {
    @Test
    fun Addition_is_passed() {
        assertEquals(4, 2 + 2)
    }
}