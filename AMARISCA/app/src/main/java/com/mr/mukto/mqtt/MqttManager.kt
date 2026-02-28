import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID


class MqttManager(
    private val gatewayId: Long,              // using DB long ID
    private val mqttGateway: String,          // actual 9999112512080001
    private val onAck: (Ack) -> Unit
) {

    private val tag = "MQTT"

    private val serverURI = "tcp://66.29.151.40:1883"
    private val clientId = "SmartSwitch-${UUID.randomUUID()}"

    private val pubTopic = "SmartSwitch/SUB/$mqttGateway"
    private val ackTopic = "SmartSwitch/ACK"

    private var client: MqttAsyncClient? = null
    private var pendingCmd: String? = null


    fun connect() {
        try {
            Log.d(tag, "Connecting → $serverURI (gateway=$mqttGateway, clientId=$clientId)")
            client = MqttAsyncClient(serverURI, clientId, null)

            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 5
                keepAliveInterval = 10
            }

            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(tag, "Connection Lost! Retrying… $cause")
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d(tag, "Message Arrived → topic=$topic payload=${message.toString()}")
                    if (topic == ackTopic) {
                        val ackMsg = message.toString()
                        val parsed = parseAck(ackMsg)
                        if (parsed != null && parsed.gatewayId == mqttGateway) {
                            Log.d(tag, "ACK Parsed OK → gw=${parsed.gatewayId} sw=${parsed.switchIndex} on=${parsed.isOn}")
                            onAck(parsed)
                        } else {
                            Log.d(tag, "ACK Ignored → $ackMsg")
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag, "Connected → Subscribing to $ackTopic")
                    client?.subscribe(ackTopic, 1)
                    pendingCmd?.let { cmd ->
                        Log.d(tag, "Sending pending command → $cmd")
                        pendingCmd = null
                        publishRaw(cmd)
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(tag, "Failed to connect: ${exception?.message}")
                }
            })

        } catch (e: Exception) {
            Log.e(tag, "Connect Exception: ${e.message}")
        }
    }


    /**
     * Publish Safe – prevents crash
     */
    fun publishSwitchCommand(switchIndex: Int, on: Boolean) {

        val cmd = if (pubTopic.endsWith("/$mqttGateway")) {
            "sw${switchIndex}:${if (on) 1 else 0}"
        } else {
            "${mqttGateway},sw${switchIndex}:${if (on) 1 else 0}"
        }
        val msg = MqttMessage(cmd.toByteArray()).apply { qos = 1 }

        val mqttClient = client

        if (mqttClient == null) {
            Log.e(tag, "Publish Blocked → client is NULL (gw=$mqttGateway)")
            return
        }

        if (!mqttClient.isConnected) {
            Log.e(tag, "Publish Blocked → client not connected (gw=$mqttGateway)")
            pendingCmd = cmd
            return
        }

        try {
            mqttClient.publish(pubTopic, msg)
            Log.d(tag, "Published → topic=$pubTopic payload=$cmd")

        } catch (e: Exception) {
            Log.e(tag, "Publish Error: ${e.message}")
        }
    }

    private fun publishRaw(cmd: String) {
        val mqttClient = client ?: return
        if (!mqttClient.isConnected) return
        try {
            val msg = MqttMessage(cmd.toByteArray()).apply { qos = 1 }
            mqttClient.publish(pubTopic, msg)
            Log.d(tag, "Published (pending) → topic=$pubTopic payload=$cmd")
        } catch (e: Exception) {
            Log.e(tag, "Publish Error (pending): ${e.message}")
        }
    }


    fun isConnected(): Boolean {
        val connected = client?.isConnected ?: false
        Log.d(tag, "isConnected($mqttGateway) → $connected")
        return connected
    }

    data class Ack(
        val gatewayId: String,
        val switchIndex: Int,
        val isOn: Boolean,
        val raw: String
    )

    private fun parseAck(payload: String): Ack? {
        // Accept formats like:
        // "9999...,sw1:1" or "ACK = 9999...,sw1:1" (with spaces)
        val regex = Regex("""(\d+)\s*,\s*sw(\d+)\s*:\s*([01])""")
        val match = regex.find(payload) ?: return null
        val gw = match.groupValues[1]
        val idx = match.groupValues[2].toIntOrNull() ?: return null
        val state = match.groupValues[3].toIntOrNull() ?: return null

        return Ack(gatewayId = gw, switchIndex = idx, isOn = state == 1, raw = payload)
    }
}
