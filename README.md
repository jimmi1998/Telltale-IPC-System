# Telltale-IPC-System

Professional Android automotive dashboard simulation showcasing an end-to-end data pipeline. Bridges C++ native hardware simulation with CRC8 checks to a Kotlin HMI via secure AIDL middleware. Features JNI integration, multi-threading, and fault-tolerant state caching.

This project implements an Android IPC-based Telltale indicator system, simulating a real-world automotive cluster environment where hardware signals are processed in the native layer and displayed on an Android UI.

## Architecture Overview

The application is built using a **3-Layer Architecture**:

1.  **Native Layer (C++/JNI):**
    *   Simulates the **Mock SPI Transport** in a dedicated background thread.
    *   Generates hardware-like frames (Start Byte, Version, ID, State, Sequence).
    *   Calculates and validates **CRC8** (Polynomial 0x07) to ensure data integrity.
    *   Uses JNI to notify the Java layer of validated state changes.

2.  **Middleware Layer (Android Service + AIDL):**
    *   A bound **Android Service** acts as the central state manager.
    *   Uses **AIDL (Android Interface Definition Language)** to allow the UI Activity (in potentially a different process) to subscribe to telltale updates.
    *   Maintains a **RemoteCallbackList** for safe and efficient multi-client updates.
    *   Caches the latest states of all telltales for new clients.

3.  **UI Layer (Activity + ViewBinding):**
    *   A modern dashboard UI with professional-looking icons (Left Turn, Right Turn, High Beam, Brake).
    *   Dynamically updates based on callbacks from the Service.
    *   Features a **Simulation Toggle** button to demonstrate two-way IPC (Activity → Service → JNI).

## How to Test

1.  **Build and Install:** Deploy the app to an Android device or emulator.
2.  **Dashboard Display:** Upon launch, the app automatically connects to the Telltale Service.
3.  **Observing Telltales:** You will see the icons (⬅, ➡, ≡D, (!)) change colors as the native simulation generates random states.
4.  **Control Simulation:** Click the **"Stop Simulation"** button to halt the native thread. Click **"Start Simulation"** to resume.

## Technical Highlights

*   **CRC8 Validation:** Ensures that only valid data frames are processed.
*   **Safe Multi-threading:** Uses `runOnUiThread` for UI updates and thread-safe JNI calls.
*   **Scalable Design:** The system is designed to handle multiple telltale IDs easily.
*   **Separation of Concerns:** Business logic is kept in the Service, hardware logic in Native, and presentation logic in the Activity.
