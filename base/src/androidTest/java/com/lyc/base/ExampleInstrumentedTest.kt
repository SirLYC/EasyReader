package com.lyc.base

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lyc.base.ui.theme.NightModeManager
import com.lyc.base.ui.theme.color_orange
import com.lyc.base.utils.blendColor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.lyc.base.test", appContext.packageName)
    }

    @Test
    fun testColor() {
        assertEquals(
            blendColor(Color.WHITE, NightModeManager.NIGHT_MODE_MASK_COLOR).toUInt().toString(16),
            ""
        )
    }

    @Test
    fun testColor2() {
        assertEquals(
            blendColor(color_orange, NightModeManager.NIGHT_MODE_MASK_COLOR).toUInt().toString(16),
            ""
        )
    }
}
