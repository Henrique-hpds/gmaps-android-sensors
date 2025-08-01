package com.henriquehpds.ic.gmapsagoravai;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;
    private List<String> inertialData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps); // hpds

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        exportInertialData(); // salva em CSV quando pausa
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            inertialData.add(timestamp + ",ACCEL," +
                    event.values[0] + "," + event.values[1] + "," + event.values[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            inertialData.add(timestamp + ",GYRO," +
                    event.values[0] + "," + event.values[1] + "," + event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void exportInertialData() {
        try {
            File file = new File(getExternalFilesDir(null), "inertial.csv");
            FileWriter writer = new FileWriter(file);
            writer.append("Timestamp,Sensor,X,Y,Z\n");

            for (String line : inertialData) {
                writer.append(line).append("\n");
            }

            writer.flush();
            writer.close();
            Toast.makeText(this, "Salvo em " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
