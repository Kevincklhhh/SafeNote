package com.example.safenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
//import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class NoteActivity extends AppCompatActivity {

    private LocationRequest locationRequest;
    private double latitude,longitude;
    EditText note,note_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        note=findViewById(R.id.note);
        note_title=findViewById(R.id.note_title);

        LocationRequest.Builder Build=new LocationRequest.Builder(5000);
        Build.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest=Build.build();

        Button addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getLocation();
            }
        });

        Button viewLocation = findViewById(R.id.view_location);
        viewLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String location= "geo:"+String.valueOf(latitude)+","+String.valueOf(longitude);
                //System.out.println(location);
                Uri gmmIntentUri = Uri.parse(location);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        Button finish = findViewById(R.id.finish_note);
        finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String content=note_title.getText().toString()+"\n"+note.getText().toString();
                StorageToInternalStorage("note.txt",content);
            }
        });

        String content="";
        content=ReadFromInternalStorage("note.txt");
        int separate=content.indexOf("\n");
        note_title.setText(content.substring(0,separate));
        note.setText(content.substring(separate+1,content.length()));

    }

    private void StorageToInternalStorage(String fileName, String content) {
        File path=getApplicationContext().getFilesDir();
        try {
            FileOutputStream writer=new FileOutputStream(new File(path,fileName));
            writer.write(content.getBytes());
            writer.close();
            Toast.makeText(getApplicationContext(),"Wrote to file: "+fileName,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String ReadFromInternalStorage(String fileName){
        File path=getApplicationContext().getFilesDir();
        File place=new File(path,fileName);
        byte[] content=new byte[(int) place.length()];
        try {
            FileInputStream reader=new FileInputStream(place);
            reader.read(content);
            return new String(content);
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }


    private boolean GPSEnable(){
        LocationManager locationManager=null;
        boolean enabled=false;

        if(locationManager==null){
            locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        }

        enabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return enabled;
    }

    private void RequestTurnOnGPS() {
        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(NoteActivity.this, "GPS is already turned on",Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(NoteActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    private void getLocation(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(NoteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                if(GPSEnable()){
                    LocationServices.getFusedLocationProviderClient(NoteActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(NoteActivity.this).removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size()>0){
                                        int index=locationResult.getLocations().size()-1;
                                        latitude=locationResult.getLocations().get(index).getLatitude();
                                        longitude=locationResult.getLocations().get(index).getLongitude();
                                        //System.out.println(latitude);
                                        //System.out.println(longitude);
                                    }
                                }
                            }, Looper.getMainLooper());
                }
                else{
                    RequestTurnOnGPS();
                }
            }
            else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
}