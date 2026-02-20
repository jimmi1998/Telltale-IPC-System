#ifndef IPC_FRAME_H
#define IPC_FRAME_H

#include <stdint.h>

#define FRAME_START_BYTE 0xA5
#define MSG_TYPE_TELLTALE 0x10
#define FRAME_VERSION 0x01

typedef struct {
    uint8_t start;
    uint8_t msg_type;
    uint8_t version;
    uint8_t flags;
    uint8_t telltale_id;
    uint8_t state;
    uint8_t seq;
    uint8_t crc;
} TelltaleFrame;

uint8_t calculate_crc8(const uint8_t *data, size_t len);

#endif // IPC_FRAME_H
