package com.brainwallet.testing
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class FlakyTest(val reason: String = "")
