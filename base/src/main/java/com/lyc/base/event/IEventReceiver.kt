package com.lyc.base.event

/**
 * Created by Liu Yuchuan on 2020/1/13.
 */
interface IEventReceiver {
    fun onEventEmit(msg: EventMessage)
}
