package com.lyc.base.event

import com.lyc.common.EventHub
import com.lyc.common.EventHubFactory
import com.lyc.common.EventHubParam
import com.lyc.common.Logger

/**
 * Created by Liu Yuchuan on 2020/1/13.
 */
class EventBus private constructor() {
    companion object {

        const val TAG = "EventBus"

        @JvmStatic
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { EventBus() }
    }

    private val eventHubMap = hashMapOf<String, EventHub<IEventReceiver>>()
    private val weakEventHubMap = hashMapOf<String, EventHub<IEventReceiver>>()

    @JvmOverloads
    fun addEventReceiver(
        event: String,
        receiver: IEventReceiver,
        weakRef: Boolean = false
    ) {
        if (weakRef) {
            weakEventHubMap.getOrPut(
                event,
                { EventHubFactory.createEventHub(EventHubParam(weakRef = true)) })
                .addEventListener(receiver)
        } else {
            eventHubMap.getOrPut(
                event,
                { EventHubFactory.createDefault(true) })
                .addEventListener(receiver)
        }
    }

    fun removeEventReceiver(
        event: String,
        receiver: IEventReceiver
    ) {
        weakEventHubMap[event]?.removeEventListener(receiver)
        eventHubMap[event]?.removeEventListener(receiver)
    }

    fun emitEvent(event: String) {
        emitEvent(EventMessage(event))
    }

    fun emitEvent(message: EventMessage) {
        Logger.i(TAG, "Emmit event: $message")
        eventHubMap[message.event]?.getEventListeners()?.forEach {
            it.onEventEmit(message)
        }
    }
}
