package com.telltale.ipc.app;

interface ITelltaleCallback {
    void onTelltaleStateChanged(int id, int state);
}
