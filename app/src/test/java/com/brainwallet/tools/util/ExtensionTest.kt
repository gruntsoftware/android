package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionTest {

    @Test
    fun `given zero, when noError, then returns true`() {
        assertTrue(0.noError())
    }

    @Test
    fun `given a non-zero value, when noError, then returns false`() {
        assertFalse(1.noError())
        assertFalse((-1).noError())
    }

    @Test
    fun `given Int Companion, when NO_ERROR, then is zero`() {
        assertEquals(0, Int.NO_ERROR)
    }

    @Test
    fun `given an array of strings, when join with separator, then joins them with that separator`() {
        assertEquals("a,b,c", String.join(arrayOf("a", "b", "c"), ','))
    }

    @Test
    fun `given a single element array, when join, then returns that element with no separator`() {
        assertEquals("only", String.join(arrayOf("only"), ','))
    }

    @Test
    fun `given an empty array, when join, then returns an empty string`() {
        assertEquals("", String.join(emptyArray(), ','))
    }

    @Test
    fun `given a lowercase word, when capitalizeFirst, then capitalizes only the first character`() {
        assertEquals("Hello", "hello".capitalizeFirst())
    }

    @Test
    fun `given an already capitalized word, when capitalizeFirst, then leaves it unchanged`() {
        assertEquals("Hello", "Hello".capitalizeFirst())
    }

    @Test
    fun `given an empty string, when capitalizeFirst, then returns an empty string`() {
        assertEquals("", "".capitalizeFirst())
    }
}
