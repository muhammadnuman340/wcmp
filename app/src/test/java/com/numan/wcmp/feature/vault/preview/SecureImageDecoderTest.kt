package com.numan.wcmp.feature.vault.preview

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class SecureImageDecoderTest {

    @Test
    fun testSampleSizeSmallImage() {
        val decoder = DefaultSecureImageDecoder()
        val method = DefaultSecureImageDecoder::class.java.getDeclaredMethod(
            "calculateInSampleSize",
            Int::class.java, Int::class.java, Int::class.java, Int::class.java
        )
        method.isAccessible = true
        val result = method.invoke(decoder, 400, 300, 1080, 1080) as Int
        assertEquals(1, result)
    }

    @Test
    fun testSampleSizeLargeImage() {
        val decoder = DefaultSecureImageDecoder()
        val method = DefaultSecureImageDecoder::class.java.getDeclaredMethod(
            "calculateInSampleSize",
            Int::class.java, Int::class.java, Int::class.java, Int::class.java
        )
        method.isAccessible = true
        val result = method.invoke(decoder, 8000, 6000, 1080, 1080) as Int
        assertEquals(4, result) // 4000x3000 (2), 2000x1500 (4) wait 8000/2=4000, 4000/2=2000 (target 1080, so 4000/2=2000 >= 1080, wait 8000/8=1000 so sample 4 is ok since 8000/4=2000, 6000/4=1500 which is > 1080)
    }
}
