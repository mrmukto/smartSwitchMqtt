# Smart Switch Gateway App

## Overview
This Android application controls **Smart Switch Gateways** using **MQTT**. It follows **MVVM architecture**, uses **Jetpack Compose** for UI, **Room DB** for persistence, and **Eclipse Paho MQTT** for real-time device communication.

The app supports:
- Multiple gateways
- Multiple switches per gateway
- Real-time switch state updates via MQTT ACK
- Hidden super-user configuration

---

## Architecture

**Pattern:** MVVM

```
UI (Jetpack Compose)
   ↓
ViewModel (StateFlow)
   ↓
Repository
   ↓
Room Database
```

MQTT communication is handled separately via `MqttManager`.

---

## Core Components

### 1. Room Database

**Entities**
- `SmartGateway`
- `SmartSwitch`

**Relations**
- One Gateway → Many Switches (`GatewayWithSwitches`)

Room ensures:
- Offline persistence
- Stable gateway & switch identity

---

### 2. ViewModel

**SmartHomeViewModel** manages:
- Gateway list
- Switch states
- Dialog visibility
- MQTT lifecycle

State is exposed via:
```kotlin
StateFlow<SmartHomeUiState>
```

---

### 3. MQTT Communication

Handled by `MqttManager`

#### Topics
```
Publish : SmartSwitch/SUB/{gatewayId}
Subscribe: SmartSwitch/ACK/{gatewayId}
```

#### Publish Payload
```
sw{index}:{0|1}
Example: sw4:1
```

#### ACK Formats Supported
```
sw3:1
9999112512080001,sw4:0
9999112512080001 = sw4:1
RESULT OK sw2:0
```

ACK parsing is **regex-based** to be firmware-safe.

---

### 4. Switch State Handling

Switch UI state is **ACK-driven** (not optimistic).

```kotlin
switchState: Map<Pair<Long, Int>, Boolean>
```

Key:
- `Long` → Gateway DB ID
- `Int` → Switch Index

This guarantees:
- UI matches physical device
- No desync

---

### 5. Hidden Super User Settings

A hidden dialog allows editing MQTT settings.

#### Trigger
- Tap **"Smart Switch Gateways"** title
- **3 taps within 3 seconds**

#### Editable Fields
- `serverURI`
- `clientId`
- Topics (optional)

Default values are used for normal users.

---

## UI Features

### Gateway
- Add
- Edit name & gateway number
- Delete gateway

### Switch
- Toggle ON/OFF
- Edit name & type
- Real-time ACK-based status

---

## Error Handling

- Safe MQTT publish (no crash if disconnected)
- Automatic reconnect
- Defensive ACK parsing
- Room-backed recovery

---

## Tech Stack

| Layer | Technology |
|------|------------|
| UI | Jetpack Compose |
| State | StateFlow |
| Architecture | MVVM |
| Database | Room |
| Networking | MQTT (Eclipse Paho) |
| Language | Kotlin |

---

## Why This Design Works

✔ Scales to many gateways
✔ Firmware-agnostic ACK parsing
✔ UI always reflects real device state
✔ Secure hidden admin configuration
✔ Crash-safe MQTT handling

---

## Future Improvements

- Switch scheduling
- Gateway health monitoring
- Role-based access
- OTA firmware control

---

## Author

Developed by **Mamunur Rashid Mukto**

Android • IoT • MQTT • NFC • MVVM

