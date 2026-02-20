package com.telltale.ipc.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log

class TelltaleService : Service(), TelltaleNative.TelltaleStateListener {

    private val callbacks = RemoteCallbackList<ITelltaleCallback>()
    private val stateCache = mutableMapOf<Int, Int>()
    private lateinit var native: TelltaleNative

    override fun onCreate() {
        super.onCreate()
        Log.i("TelltaleService", "Service started")
        native = TelltaleNative(this)
        native.startTransport()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        native.stopTransport()
        callbacks.kill()
        super.onDestroy()
    }

    override fun onStateChanged(id: Int, state: Int) {
        if (stateCache[id] != state) {
            stateCache[id] = state
            notifyClients(id, state)
        }
    }

    private fun notifyClients(id: Int, state: Int) {
        val n = callbacks.beginBroadcast()
        for (i in 0 until n) {
            try {
                callbacks.getBroadcastItem(i).onTelltaleStateChanged(id, state)
            } catch (e: Exception) {
                Log.e("TelltaleService", "Broadcast error", e)
            }
        }
        callbacks.finishBroadcast()
    }

    private val binder = object : ITelltaleService.Stub() {
        override fun getTelltaleState(id: Int): Int {
            return stateCache[id] ?: 0
        }

        override fun registerCallback(callback: ITelltaleCallback?) {
            if (callback != null) {
                callbacks.register(callback)
                stateCache.forEach { (id, state) ->
                    try {
                        callback.onTelltaleStateChanged(id, state)
                    } catch (e: Exception) {}
                }
            }
        }

        override fun unregisterCallback(callback: ITelltaleCallback?) {
            if (callback != null) {
                callbacks.unregister(callback)
            }
        }

        override fun setSimulationEnabled(enabled: Boolean) {
            if (enabled) native.startTransport() else native.stopTransport()
        }

        override fun pauseSimulation(pause: Boolean) {
            native.setPause(pause)
        }
    }
}
