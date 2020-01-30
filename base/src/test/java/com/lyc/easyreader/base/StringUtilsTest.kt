package com.lyc.easyreader.base

import com.lyc.easyreader.base.utils.getMd5
import org.junit.Assert
import org.junit.Test

/**
 * Created by Liu Yuchuan on 2020/1/15.
 */
class StringUtilsTest {

    @Test
    fun testMd5() {
        Assert.assertEquals(
            "005732AE53F480B744BF3C1384AFF25C",
            "sdfasdfsadf".getMd5().toUpperCase()
        )
        Assert.assertEquals(
            "2B43DC5693A1DF8A88454F8EAC5FEA98",
            "weurikwyheuiw".getMd5().toUpperCase()
        )
    }

}
