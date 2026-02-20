package com.telltale.ipc.app

import android.util.Log

class TelltaleNative(private val listener: TelltaleStateListener) {

    interface TelltaleStateListener {
        fun onStateChanged(id: Int, state: Int)
    }

    init {
        System.loadLibrary("app")
    }

    external fun startTransport()
    external fun stopTransport()
    external fun setPause(pause: Boolean)

    // Called from JNI
    private fun onStateChanged(id: Int, state: Int) {
        Log.i("NativeIPC", "State Update: ID=$id, State=$state")
        listener.onStateChanged(id, state)
    }
}
