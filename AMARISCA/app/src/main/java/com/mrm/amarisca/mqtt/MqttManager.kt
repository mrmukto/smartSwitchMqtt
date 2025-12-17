import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import java.util.UUID


class MqttManager(
    private val gatewayId: Long,              // using DB long ID
    private val mqttGateway: String,          // actual 9999112512080001
    private val onAck: (String) -> Unit
) {

    private val serverURI = "URL"
    private val clientId = "SmartSwitch-${UUID.randomUUID()}"

    private val pubTopic = "SmartSwitch/SUB/$mqttGateway"
    private val ackTopic = "SmartSwitch/ACK/$mqttGateway"

    private var client: MqttAsyncClient? = null


    fun connect() {
        try {
            client = MqttAsyncClient(serverURI, clientId, null)

            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 5
                keepAliveInterval = 10
            }

            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Connection Lost! Retrying… $cause")
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    if (topic == ackTopic) {
                        val ackMsg = message.toString()
                        Log.d("MQTT", "ACK Received: $ackMsg")
                        onAck(ackMsg)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Connected → Subscribing to $ackTopic")
                    client?.subscribe(ackTopic, 1)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Failed to connect: ${exception?.message}")
                }
            })

        } catch (e: Exception) {
            Log.e("MQTT", "Connect Exception: ${e.message}")
        }
    }


    /**
     * Publish Safe – prevents crash
     */
    fun publishSwitchCommand(switchIndex: Int, on: Boolean) {

        val cmd = "sw${switchIndex}:${if (on) 1 else 0}"
        val msg = MqttMessage(cmd.toByteArray()).apply { qos = 1 }

        val mqttClient = client

        if (mqttClient == null) {
            Log.e("MQTT", "Client NULL → cannot publish")
            return
        }

        if (!mqttClient.isConnected) {
            Log.e("MQTT", "Client NOT connected → cannot publish yet")
            return
        }

        try {
            mqttClient.publish(pubTopic, msg)
            Log.d("MQTT", "Published → $pubTopic : $cmd")

        } catch (e: Exception) {
            Log.e("MQTT", "Publish Error: ${e.message}")
        }
    }


    fun isConnected(): Boolean {
        return client?.isConnected ?: false
    }
}
