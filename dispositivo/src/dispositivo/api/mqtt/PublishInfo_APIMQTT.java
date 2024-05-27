package dispositivo.api.mqtt;

import dispositivo.api.iot.infraestructure.Dispositivo_RegistradorMQTT;
import dispositivo.interfaces.Configuracion;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;
import dispositivo.utils.MySimpleLogger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class PublishInfo_APIMQTT implements MqttCallback{

    protected MqttClient myClient;
    protected MqttConnectOptions connOpt;

    protected String dispositivoId = null;
    protected String dispositivoIP = null;
    protected String mqttBroker = null;

    private String loggerId = null;


    public static PublishInfo_APIMQTT build(String dispositivoId, String dispositivoIP, String mqttBroker) {
        PublishInfo_APIMQTT reg = new PublishInfo_APIMQTT();
        reg.setDispositivoID(dispositivoId);
        reg.setDispositivoIP(dispositivoIP);
        reg.setBrokerURL(mqttBroker);
        return reg;
    }

    protected PublishInfo_APIMQTT() {
    }

    protected void setDispositivoID(String dispositivoID) {
        this.dispositivoId = dispositivoID;
        this.loggerId = dispositivoID + "-RegisterService";
    }

    protected void setDispositivoIP(String dispositivoIP) {
        this.dispositivoIP = dispositivoIP;
    }

    protected void setBrokerURL(String brokerURL) {
        this.mqttBroker = brokerURL;
    }

    @Override
    public void connectionLost(Throwable t) {
        MySimpleLogger.warn(this.loggerId, "Connection lost!");
        // code to reconnect to the broker would go here if desired
    }

    /**
     *
     * deliveryComplete
     * This callback is invoked when a message published by this client
     * is successfully received by the broker.
     *
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //System.out.println("Pub complete" + new String(token.getMessage().getPayload()));
    }

    /**
     *
     * messageArrived
     * This callback is invoked when a message is received on a subscribed topic.
     *
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    }



    public void connect() {

        // setup MQTT Client
        String clientID = this.dispositivoId + UUID.randomUUID().toString();
        connOpt = new MqttConnectOptions();

        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(30);
//			connOpt.setUserName(M2MIO_USERNAME);
//			connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

        // Connect to Broker
        try {

            MqttDefaultFilePersistence persistence = null;
            try {
                persistence = new MqttDefaultFilePersistence("/tmp");
            } catch (Exception e) {
            }
            if ( persistence != null )
                myClient = new MqttClient(this.mqttBroker, clientID, persistence);
            else
                myClient = new MqttClient(this.mqttBroker, clientID);

            myClient.setCallback(this);
            myClient.connect(connOpt);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        MySimpleLogger.info(this.loggerId, "Conectado al broker " + this.mqttBroker);

    }


    public void disconnect() {
        // disconnect
        try {
            myClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void publishFunctionInfo(IFuncion funcion) {
        if (this.myClient == null || !this.myClient.isConnected()) {
            this.connect();
        }

        MqttTopic topic = myClient.getTopic(Configuracion.TOPIC_BASE + "dispositivo/" + dispositivoId + "/funcion/" + funcion.getId() + "/info");


        JSONObject pubMsg = new JSONObject();
        JSONObject funcionMsg = new JSONObject();
        try {
            pubMsg.put("dispositivo", dispositivoId);
            funcionMsg.put("id", funcion.getId());
            funcionMsg.put("estado", funcion.getStatus());
            pubMsg.put("funcion", funcionMsg);
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        int pubQoS = 0;
        MqttMessage message = new MqttMessage(pubMsg.toString().getBytes());
        message.setQos(pubQoS);
        message.setRetained(false);

        // Publish the message
        MySimpleLogger.debug(this.loggerId, "Publicando en topic \"" + topic + "\" qos " + pubQoS);
        MqttDeliveryToken token = null;
        try {
            // publish message to broker
            token = topic.publish(message);
            MySimpleLogger.debug(this.loggerId, pubMsg.toString());
            // Wait until the message has been delivered to the broker
            token.waitForCompletion();
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void desregistrar() {

        // ToDo


    }

}
