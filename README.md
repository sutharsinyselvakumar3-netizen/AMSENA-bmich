# AI COMPANION – Smart Onion Garden Robot

> **“See the Weed. Protect the Onion. Work Smart.”**

Professional hardware control and telemetry monitoring Android application designed for real agricultural onion garden robots powered by an **ESP32 38-Pin Development Board** and an **AI-Thinker ESP32-CAM**.

---

## Hardware Architecture

### 1. Main Controller
* **ESP32 38-pin Dev Board** (Coordinates all motor drivers, servos, sensor polling, and hardware safety interlocks).

### 2. Camera System
* **AI-Thinker ESP32-CAM + OV2640**
* MJPEG Video Stream: `http://<ESP32_CAM_IP>:81/stream`
* Frame Capture Endpoint: `http://<ESP32_CAM_IP>/capture`

### 3. Sensors
1. **TCS34725 RGB Color Sensor** (I2C SDA: GPIO 21, SCL: GPIO 22) – Onion disease colour indication (Fusarium Basal Rot, Purple Blotch, Downy Mildew, Neck Rot).
2. **DHT11 Climate Sensor** (1-Wire DATA: GPIO 4) – Ambient temperature & relative humidity.
3. **Capacitive Soil Moisture Sensor v1.2** (Analog: GPIO 34 ADC1_CH6) – Soil moisture %.
4. **Waterproof Ultrasonic Sensor JSN-SR04T** (ECHO: GPIO 36, TRIG: GPIO 5) – Water and spray tank liquid levels.
5. **Battery Voltage Sensor** (Resistor Divider Analog: GPIO 35 ADC1_CH7) – Li-ion/LiFePO4 pack monitoring.
6. **Physical Emergency Stop Switch** (Digital Interrupt: GPIO 39) – Hardware safety cutoff.

### 4. Actuators
1. **Left DC Motor**: IN1 (GPIO 25), IN2 (GPIO 26), PWM (GPIO 27)
2. **Right DC Motor**: IN3 (GPIO 32), IN4 (GPIO 33), PWM (GPIO 23)
3. **Servo 1**: Camera PAN (GPIO 13)
4. **Servo 2**: Camera TILT (GPIO 14)
5. **Servo 3**: 3-DOF Arm BASE (GPIO 16)
6. **Servo 4**: 3-DOF Arm SHOULDER (GPIO 17)
7. **Servo 5**: Cutter Position (GPIO 18)
8. **Servo 6**: Soil Sensor Deployment (GPIO 19, 0° HOME, 90° DEPLOYED)
9. **Cutter Motor Driver**: MOSFET on GPIO 2 (7s safety maximum enforced in firmware)
10. **Spray Pump Driver**: Relay/MOSFET on GPIO 12 (30s duration)
11. **Water Irrigation Pump**: Relay on GPIO 15 (30s duration)
12. **Audio Buzzer Driver**: Transistor on GPIO 0

---

## ESP32 GPIO Pinout Reference Table

| GPIO | Component | Type | Function |
| ---: | :--- | :--- | :--- |
| **25** | Left Motor IN1 | Motor DIR | Direction A |
| **26** | Left Motor IN2 | Motor DIR | Direction B |
| **27** | Left Motor PWM | Motor PWM | Speed PWM |
| **32** | Right Motor IN3 | Motor DIR | Direction A |
| **33** | Right Motor IN4 | Motor DIR | Direction B |
| **23** | Right Motor PWM | Motor PWM | Speed PWM |
| **13** | Servo 1 Camera PAN | PWM Servo | Left / Center / Right |
| **14** | Servo 2 Camera TILT | PWM Servo | Up / Center / Down |
| **16** | Servo 3 Arm BASE | PWM Servo | Base Rotation |
| **17** | Servo 4 Arm SHOULDER | PWM Servo | Shoulder Joint |
| **18** | Servo 5 Cutter Position | PWM Servo | End-Effector Positioning |
| **19** | Servo 6 Soil Sensor | PWM Servo | 0° Retracted, 90° Planted |
| **21** | TCS34725 SDA | I2C Data | 400kHz I2C Bus |
| **22** | TCS34725 SCL | I2C Clock | 400kHz I2C Bus |
| **4** | DHT11 DATA | 1-Wire Digital | Temp/Humidity |
| **34** | Soil Sensor Analog | ADC1_CH6 | Soil Moisture |
| **35** | Battery Voltage Analog | ADC1_CH7 | Voltage Divider (12V) |
| **36** | Ultrasonic ECHO | Digital Input | Liquid Echo Return |
| **5** | Ultrasonic TRIG | Digital Output | 10us Trigger Pulse |
| **2** | Cutter Driver | Output MOSFET | Mechanical Weed Cutter |
| **12** | Sprayer Driver | Output Relay | Foliage Treatment Pump |
| **15** | Water Pump Driver | Output Relay | Soil Watering Pump |
| **0** | Buzzer Driver | Output Transistor | Audio Alarm & Fault Warning |
| **39** | Emergency Stop Input | Digital Interrupt | NC Physical E-Stop Button |

---

## REST API Specification

### Endpoints (ESP32 Controller)
* `GET /api/status` – Full real-time telemetry JSON payload.
* `POST /api/mode` – Change mode `{"mode": "MANUAL" | "AUTO"}`.
* `POST /api/robot` – Drive command `{"direction": "FORWARD"|"REVERSE"|"LEFT"|"RIGHT"|"STOP"}`.
* `POST /api/speed` – Speed level `{"speed": "SLOW"|"NORMAL"|"FAST"}`.
* `POST /api/servo` – Camera servo gimbal `{"servoId": 1|2, "position": "LEFT"|"CENTER"|"RIGHT"|"UP"|"DOWN"}`.
* `POST /api/buzzer` – Manual buzzer toggle `{"state": true|false}`.
* `POST /api/auto/start` – Engage autonomous weed removal traversal.
* `POST /api/auto/stop` – Halt autonomous traversal and disengage actuators.
* `POST /api/emergency_stop` – Software emergency halt.

---

## Automated Sequences & Safety Rules

1. **Pre-Flight Safety Check**: Verifies 13 hardware prerequisites before entering AUTO mode.
2. **Weed Removal**:
   - `ONION`: Never cut ("ONION DETECTED - DO NOT CUT", continues traversal).
   - `UNKNOWN`: Ignored.
   - `WEED`: Confirmed with configurable confidence threshold and consecutive frame detections -> Robot stops -> Image coordinates calculated -> Arm moves -> Cutter engages for maximum 7 seconds -> Arm returns to HOME -> Traversal resumes at SLOW speed.
3. **Disease Colour Indication (TCS34725)**:
   - Evaluates real-time RGB values against calibrated profiles for **Fusarium Basal Rot**, **Purple Blotch**, **Downy Mildew**, and **Neck Rot**.
   - Requires consecutive confirmation readings.
   - Stops robot, checks spray liquid, sprays target foliage for 30s.
   - Prominently displays: *“Use only an appropriate, legally permitted product according to its label and local agricultural guidance.”*
4. **Soil + DHT11 5-Minute Routine**:
   - Countdown timer (05:00).
   - Robot stops, Servo 6 deploys to 90°, checks moisture.
   - If moisture < threshold and tank has water: 30s irrigation pump -> Retracts to 0° -> Resumes.
5. **Actuator Priority Hierarchy**:
   `E-STOP > CRITICAL BATTERY > WEED CUTTING > DISEASE SPRAYING > SOIL WATERING > SLOW TRAVERSAL`
