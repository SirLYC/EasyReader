package com.lyc.easyreader.bookshelf.reader.page.anim

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
enum class PageAnimMode(
    val displayName: String
) {
    SIMULATION("仿真"),
    COVER("覆盖"),
    SLIDE("滑动"),
    CUBIC("立方"),
    ROTATE_3D("旋转"),
    TURN_TABLE("转盘"),
    TURN_PAGE("翻页"),
    FADE_OUT_COVER("淡出覆盖"),
    FADE_IN_FATE_OUT("淡入淡出"),
    NONE("无"),
}
