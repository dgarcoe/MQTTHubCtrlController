package com.github.dgarcoe.mqtthubctrlcontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    MqttAndroidClient mqttAndroidClient;
    String brokerURL;
    String user;
    String password;
    int hub;
    int port;
    boolean power;
    final String cliendID = "MQTTHubCtrlController-Android";

    EditText eBrokerURL;
    EditText eUser;
    EditText ePassword;
    EditText eHub;
    EditText ePort;
    ToggleButton buttonPower;
    Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eBrokerURL = (EditText)findViewById(R.id.editTextBroker);
        eUser = (EditText)findViewById(R.id.editTextUser);
        ePassword = (EditText)findViewById(R.id.editTextPwd);
        eHub = (EditText)findViewById(R.id.editTextHub);
        ePort = (EditText)findViewById(R.id.editTextPort);
        buttonPower = (ToggleButton)findViewById(R.id.toggleButtonPower);
        buttonSend = (Button)findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brokerURL = eBrokerURL.getText().toString();
                user = eUser.getText().toString();
                password = ePassword.getText().toString();
                hub = Integer.parseInt(eHub.getText().toString());
                port = Integer.parseInt(ePort.getText().toString());
                power = buttonPower.isChecked();

                if (brokerURL.equals("") || user.equals("") || password.equals("")) {
                    Toast.makeText(MainActivity.this, "Please fill all the fields to connect!",Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MQTT","Connecting to broker");
                    mqttAndroidClient = new MqttAndroidClient(MainActivity.this.getApplicationContext(),"tcp://"+brokerURL,cliendID);
                    mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                        @Override
                        public void connectComplete(boolean reconnect, String serverURI) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("hub",hub);
                                jsonObject.put("port", port);
                                jsonObject.put("power",power);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                mqttAndroidClient.publish("power",new MqttMessage(jsonObject.toString().getBytes()));
                                mqttAndroidClient.disconnect();
                                Toast.makeText(MainActivity.this, "Message sent!",Toast.LENGTH_SHORT).show();
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });
                    connect();
                }
            }
        });


    }

    private void connect(){

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(user);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Couldn't connect to MQTT broker",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            Log.e("MQTTHubCtrlController","MQTT Exception");
        }


    }
}
