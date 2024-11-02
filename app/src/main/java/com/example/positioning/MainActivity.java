package com.example.positioning;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] gravity;
    private float[] geomagnetic;

    private final String udpAddress = "10.252.93.103"; // Replace with your server IP
    private final int udpPort = 4999; // Replace with your server port
    private boolean shouldSendData = false;
    public Button sendDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Initialize SensorManager and sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Set up the button and its click listener
        sendDataButton = findViewById(R.id.sendDataButton);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listeners to save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get sensor data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone();
        }

        // Calculate orientation if both gravity and geomagnetic data are available
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                // Azimuth is the direction in radians, convert it to degrees
                float azimuthInDegrees = (float) Math.toDegrees(orientation[0]);
                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360; // Ensure the azimuth is positive
                }

                float angle2InDegrees = (float) Math.toDegrees(orientation[1]);
                if (angle2InDegrees < 0) {
                    angle2InDegrees += 360; // Ensure the azimuth is positive
                }

                float angle3InDegrees = (float) Math.toDegrees(orientation[2]);
                if (angle3InDegrees < 0) {
                    angle3InDegrees += 360; // Ensure the azimuth is positive
                }


                // Send azimuth over UDP


                sendUDP(azimuthInDegrees, angle2InDegrees, angle3InDegrees,sendDataButton.isPressed());


            }
        }
    }

    private void sendUDP(final float azimuth,final float angle2,final float angle3, boolean isPressed) {
        // Sending UDP in a new thread to avoid blocking the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    String message = "";
                    if (isPressed) {
                         message = String.valueOf(azimuth) + "," + String.valueOf(angle2) + "," + String.valueOf(angle3) + ";1";
                    } else {
                         message = String.valueOf(azimuth) + "," + String.valueOf(angle2) + "," + String.valueOf(angle3)+";0";
                    }
                    byte[] buffer = message.getBytes();

                    InetAddress address = InetAddress.getByName(udpAddress);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, udpPort);

                    socket.send(packet);
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendFire() {
        // Sending UDP in a new thread to avoid blocking the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    String message = "Fire";
                    byte[] buffer = message.getBytes();

                    InetAddress address = InetAddress.getByName(udpAddress);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, udpPort);

                    socket.send(packet);
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not implemented
    }
}
