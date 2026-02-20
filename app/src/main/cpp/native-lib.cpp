#include <jni.h>
#include <string>
#include <vector>
#include <thread>
#include <chrono>
#include <android/log.h>
#include "ipc_frame.h"

#define TAG "NativeIPC"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

uint8_t crc8(const uint8_t *data, size_t len) {
    uint8_t crc = 0x00;
    for (size_t i = 0; i < len; i++) {
        crc ^= data[i];
        for (int j = 0; j < 8; j++) {
            if (crc & 0x80) {
                crc = (crc << 1) ^ 0x07;
            } else {
                crc <<= 1;
            }
        }
    }
    return crc;
}

static JavaVM* jvm = nullptr;
static jobject callback_ref = nullptr;
static jmethodID mid_on_state_changed = nullptr;
static bool running = false;
static bool paused = false;

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    jvm = vm;
    return JNI_VERSION_1_6;
}

void update_java(uint8_t id, uint8_t state) {
    if (jvm && callback_ref && mid_on_state_changed) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, nullptr) == JNI_OK) {
            env->CallVoidMethod(callback_ref, mid_on_state_changed, (jint)id, (jint)state);
            jvm->DetachCurrentThread();
        }
    }
}

void simulation_loop() {
    LOGI("Simulation thread started");
    uint8_t seq = 0;
    while (running) {
        if (paused) {
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
            continue;
        }

        for (uint8_t id = 1; id <= 4; id++) {
            if (!running || paused) break;

            TelltaleFrame frame;
            frame.start = FRAME_START_BYTE;
            frame.msg_type = MSG_TYPE_TELLTALE;
            frame.version = FRAME_VERSION;
            frame.flags = 0;
            frame.telltale_id = id;
            frame.state = (rand() % 2);
            frame.seq = seq++;
            frame.crc = crc8((uint8_t*)&frame, 7);

            if (crc8((uint8_t*)&frame, 7) == frame.crc) {
                update_java(frame.telltale_id, frame.state);
            }

            std::this_thread::sleep_for(std::chrono::milliseconds(500));
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_telltale_ipc_app_TelltaleNative_startTransport(JNIEnv* env, jobject thiz) {
    if (running) return;

    callback_ref = env->NewGlobalRef(thiz);
    jclass cls = env->GetObjectClass(thiz);
    mid_on_state_changed = env->GetMethodID(cls, "onStateChanged", "(II)V");

    running = true;
    paused = false;
    std::thread(simulation_loop).detach();
}

extern "C" JNIEXPORT void JNICALL
Java_com_telltale_ipc_app_TelltaleNative_stopTransport(JNIEnv* env, jobject thiz) {
    running = false;
    if (callback_ref) {
        env->DeleteGlobalRef(callback_ref);
        callback_ref = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_telltale_ipc_app_TelltaleNative_setPause(JNIEnv* env, jobject thiz, jboolean p) {
    paused = (bool)p;
}
