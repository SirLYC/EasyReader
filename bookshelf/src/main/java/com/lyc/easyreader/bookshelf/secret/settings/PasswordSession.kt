package com.lyc.easyreader.bookshelf.secret.settings

/**
 * Created by Liu Yuchuan on 2020/3/9.
 */
enum class PasswordSession(
    val duration: Long,
    val displayName: String
) {
    Never(-1L, "每次都需要输入"),
    Seconds15(15L, "15秒"),
    Seconds30(30L, "30秒"),
    Minute1(60L, "1分钟"),
    Minute5(300L, "5分钟"),
    Minute10(600L, "10分钟"),
    Minute15(900L, "15分钟"),
    Minute30(1800L, "30分钟")
}
