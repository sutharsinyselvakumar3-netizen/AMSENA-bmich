package com.example.data.model

/**
 * ESP32 38-pin Hardware Pin Configuration
 * Exact mapping for documentation, diagnostics, and hardware verification.
 * Note: Android application does NOT control GPIO directly;
 * the ESP32 safety firmware is the final hardware safety controller.
 */
data class PinMapping(
    val gpio: Int,
    val component: String,
    val type: PinType,
    val description: String
)

enum class PinType {
    MOTOR_PWM,
    MOTOR_DIR,
    SERVO,
    I2C,
    ONE_WIRE,
    ANALOG_IN,
    ULTRASONIC,
    OUTPUT_DRIVER,
    DIGITAL_IN
}

object HardwarePinConfig {
    val PINS = listOf(
        PinMapping(25, "Left Motor IN1", PinType.MOTOR_DIR, "Direction control for Left DC motor driver"),
        PinMapping(26, "Left Motor IN2", PinType.MOTOR_DIR, "Direction control for Left DC motor driver"),
        PinMapping(27, "Left Motor PWM", PinType.MOTOR_PWM, "PWM speed control for Left DC motor"),
        PinMapping(32, "Right Motor IN3", PinType.MOTOR_DIR, "Direction control for Right DC motor driver"),
        PinMapping(33, "Right Motor IN4", PinType.MOTOR_DIR, "Direction control for Right DC motor driver"),
        PinMapping(23, "Right Motor PWM", PinType.MOTOR_PWM, "PWM speed control for Right DC motor"),
        PinMapping(13, "Servo 1 Camera PAN", PinType.SERVO, "Controls horizontal camera angle (Left/Center/Right)"),
        PinMapping(14, "Servo 2 Camera TILT", PinType.SERVO, "Controls vertical camera angle (Up/Center/Down)"),
        PinMapping(16, "Servo 3 Arm BASE", PinType.SERVO, "3-DOF Arm Base rotation servo"),
        PinMapping(17, "Servo 4 Arm SHOULDER", PinType.SERVO, "3-DOF Arm Shoulder joint servo"),
        PinMapping(18, "Servo 5 Cutter Position", PinType.SERVO, "End-effector cutter positioning servo"),
        PinMapping(19, "Servo 6 Soil Sensor", PinType.SERVO, "Deployment servo for soil moisture sensor (0°-90°)"),
        PinMapping(21, "TCS34725 SDA", PinType.I2C, "I2C Data line for RGB Color Sensor"),
        PinMapping(22, "TCS34725 SCL", PinType.I2C, "I2C Clock line for RGB Color Sensor"),
        PinMapping(4, "DHT11 DATA", PinType.ONE_WIRE, "Ambient temperature and humidity 1-wire digital bus"),
        PinMapping(34, "Soil Sensor Analog", PinType.ANALOG_IN, "Capacitive soil moisture analog input ADC1_CH6"),
        PinMapping(35, "Battery Voltage Analog", PinType.ANALOG_IN, "Resistor divider battery voltage input ADC1_CH7"),
        PinMapping(36, "Ultrasonic ECHO", PinType.ULTRASONIC, "Waterproof liquid level sensor ECHO receiver"),
        PinMapping(5, "Ultrasonic TRIG", PinType.ULTRASONIC, "Waterproof liquid level sensor TRIGGER pulse"),
        PinMapping(2, "Cutter Driver", PinType.OUTPUT_DRIVER, "MOSFET driver for mechanical weed cutter motor (max 7s)"),
        PinMapping(12, "Sprayer Driver", PinType.OUTPUT_DRIVER, "Relay/MOSFET driver for liquid spray pump (30s)"),
        PinMapping(15, "Water Pump Driver", PinType.OUTPUT_DRIVER, "Relay driver for soil irrigation water pump (30s)"),
        PinMapping(0, "Buzzer Driver", PinType.OUTPUT_DRIVER, "Audio warning buzzer for faults and emergency"),
        PinMapping(39, "Emergency Stop Input", PinType.DIGITAL_IN, "Physical normally-closed E-Stop button with hardware interrupt")
    )
}
