package com.henriquehpds.ic.gmapsagoravai;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final ArrayList<String> sensorData = new ArrayList<>();
    private final ArrayList<String> locationData = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    long timestamp = System.currentTimeMillis();
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    locationData.add(timestamp + "," + lat + "," + lon);
                }
            }
        };

        requestLocationPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Exemplo: adicionar marcador em uma posição fixa
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
    }

    private void requestLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);  // 2 segundos
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        Log.d("TESTE", "Teste de sanidade mental 2");
        super.onPause();
        sensorManager.unregisterListener(this);
        saveDataToCSV();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorData.add(timestamp + ",ACCEL," + event.values[0] + "," + event.values[1] + "," + event.values[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            sensorData.add(timestamp + ",GYRO," + event.values[0] + "," + event.values[1] + "," + event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveDataToCSV();
    }

    private void saveDataToCSV() {

        Log.d("TESTE", "Teste de sanidade mental");

        File dir = getExternalFilesDir(null)
        if (dir != null && !dir.exists()) dir.mkdirs();

        File sensorFile = new File(dir, "sensor_data.csv");
        File locationFile = new File(dir, "location_data.csv");

        try {
            FileWriter sensorWriter = new FileWriter(sensorFile);
            sensorWriter.write("timestamp,type,x,y,z\n");
            for (String line : sensorData) {
                sensorWriter.write(line + "\n");
            }
            sensorWriter.close();

            FileWriter locationWriter = new FileWriter(locationFile);
            locationWriter.write("timestamp,latitude,longitude\n");
            for (String line : locationData) {
                locationWriter.write(line + "\n");
            }
            locationWriter.close();

            Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_LONG).show();

            Log.d("CSV", "Arquivo salvo em: " + dir.getAbsolutePath());


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar dados.", Toast.LENGTH_LONG).show();
        }
    }
}
