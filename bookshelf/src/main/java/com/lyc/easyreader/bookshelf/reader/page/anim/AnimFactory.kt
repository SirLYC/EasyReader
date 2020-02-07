package com.lyc.easyreader.bookshelf.reader.page.anim

import android.view.View

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
object AnimFactory {
    fun createAnim(
        animMode: PageAnimMode,
        screenWidth: Int,
        screenHeight: Int,
        view: View,
        listener: PageAnimation.OnPageChangeListener
    ): PageAnimation? {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return null
        }
        return when (animMode) {
            PageAnimMode.COVER -> CoverPageAnim(screenWidth, screenHeight, view, listener)
            PageAnimMode.SLIDE -> SlidePageAnim(screenWidth, screenHeight, view, listener)
            PageAnimMode.NONE -> NonePageAnim(screenWidth, screenHeight, view, listener)
            PageAnimMode.SIMULATION -> SimulationPageAnim(screenWidth, screenHeight, view, listener)
            PageAnimMode.CUBIC -> CubicPageAnim(screenWidth, screenHeight, view, listener)
        }
    }
}
