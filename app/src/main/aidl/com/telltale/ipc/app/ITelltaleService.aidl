package com.telltale.ipc.app;

import com.telltale.ipc.app.ITelltaleCallback;

interface ITelltaleService {
    int getTelltaleState(int id);
    void registerCallback(ITelltaleCallback callback);
    void unregisterCallback(ITelltaleCallback callback);
    void setSimulationEnabled(boolean enabled);
    void pauseSimulation(boolean pause);
}
