
package study.iot.tb.demo_client.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MsgHandler {

    void onMessage(String type, MqttMessage data);

    void onEvent(int event);

}
