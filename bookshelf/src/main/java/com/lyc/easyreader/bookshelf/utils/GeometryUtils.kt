package com.lyc.easyreader.bookshelf.utils

import android.graphics.PointF

/**
 * Created by Liu Yuchuan on 2020/2/5.
 */
fun getCross(p1: PointF, p2: PointF, p3: PointF, p4: PointF): PointF? {
    if (p1.x == p2.x) {
        if (p3.x == p4.x) {
            // 同时垂直于X轴，没有交点
            return null
        }

        return PointF(p1.x, (p3.y - p4.y) / (p3.x - p4.x) * (p1.x - p3.x) + p3.y)
    }

    if (p3.x == p4.x) {
        return getCross(p3, p4, p1, p2)
    }

    val k1 = (p2.y - p1.y) / (p2.x - p1.x)
    val k2 = (p4.y - p3.y) / (p4.x - p3.x)

    // 平行，无交点
    if (k1 == k2) {
        return null
    }

    val crossP = PointF()
    // 二元函数通式： y=ax+b
    val b1 = (p1.x * p2.y - p2.x * p1.y) / (p1.x - p2.x)
    val b2 = (p3.x * p4.y - p4.x * p3.y) / (p3.x - p4.x)
    crossP.x = (b2 - b1) / (k1 - k2)
    crossP.y = k1 * crossP.x + b1
    return crossP
}
