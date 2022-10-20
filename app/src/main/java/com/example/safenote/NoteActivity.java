package com.example.safenote;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
//import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class NoteActivity extends AppCompatActivity {

    private LocationRequest locationRequest;
    private double latitude, longitude;
    EditText note, note_title;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey generateSecretKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(new KeyGenParameterSpec.Builder("NoteKey",
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return keyGenerator.generateKey();
    }

    private SecretKey getKey() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, UnrecoverableEntryException {
        KeyStore ks = KeyStore.getInstance("AndroidKeystore");
        ks.load(null);
        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) ks.getEntry("NoteKey", null);
        if (secretKeyEntry.getSecretKey().equals(null))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return generateSecretKey();
            }
        return secretKeyEntry.getSecretKey();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        note = findViewById(R.id.note);
        note_title = findViewById(R.id.note_title);

        LocationRequest.Builder Build = new LocationRequest.Builder(5000);
        Build.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest = Build.build();

//        SecretKey k = null;
        KeyManager keyManager = new KeyManager("noteIV");
//        try {
//            k = keyManager.getKey();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
        KeyManager kmlat = new KeyManager("latIV");
        KeyManager kmlong = new KeyManager("longIV");
        Button addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getLocation();
            }
        });

        Button viewLocation = findViewById(R.id.view_location);
        viewLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String storedLatitude = sh.getString("latitude", "");
                String storedLongitude = sh.getString("longitude", "");
                String decryptedlatitude = null;
                String decryptedlongitude = null;
                try {
                    decryptedlatitude = new String(kmlat.decrypt(getApplicationContext(), Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT)));
                    decryptedlongitude = new String(kmlong.decrypt(getApplicationContext(), Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT)));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                latitude = Double.parseDouble(decryptedlatitude);
                longitude = Double.parseDouble(decryptedlongitude);
                String location = "geo:" + String.valueOf(latitude) + "," + String.valueOf(longitude);
                //System.out.println(location);
                Uri gmmIntentUri = Uri.parse(location);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        Button finish = findViewById(R.id.finish_note);
//        SecretKey finalK = k;
        finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String content = note_title.getText().toString() + "\n" + note.getText().toString();
                if (note_title.getText().toString().indexOf('\n') >= 0) {
                    Toast.makeText(getApplicationContext(), "Save failed: Title cannot contain multiple lines!", Toast.LENGTH_SHORT).show();
                    return;
                }
//                StorageToInternalStorage("note.txt", content);
                try {
                    byte[] encrypted = keyManager.encrypt(getApplicationContext(), content.getBytes(StandardCharsets.UTF_8));
                    StorageToInternalStorage("note", encrypted);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        // decode
        String content = "\n";
        try {
            byte[] encrypted = ReadFromInternalStorage("note");
            if (encrypted == null) content = "\n";
            else
                content = new String(keyManager.decrypt(getApplicationContext(), encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("content is" + content);
        if (content.length() == 0) content = "\n";
        int separate = content.indexOf("\n");
        if (separate < 0) {
            System.out.println("Decode fails");
            content = "\n";
            separate = 0;
        }
        System.out.println(content);
        note_title.setText(content.substring(0, separate));
        note.setText(content.substring(separate + 1, content.length()));

    }

    private void StorageToInternalStorage(String fileName, byte[] content) {
        File path = getApplicationContext().getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
//            writer.write(content.getBytes());
            writer.write(content);
            writer.close();
            Toast.makeText(getApplicationContext(), "Wrote to file: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] ReadFromInternalStorage(String fileName) throws IOException {
        File path = getApplicationContext().getFilesDir();
        File place = new File(path, fileName);
        byte[] content = new byte[(int) place.length()];
        if (!place.isFile() && !place.createNewFile()) {
            throw new IOException("Error creating new file: " + place.getAbsolutePath());
        }
        try {
            FileInputStream reader = new FileInputStream(place);
            reader.read(content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean GPSEnable() {
        LocationManager locationManager = null;
        boolean enabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return enabled;
    }

    private void RequestTurnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(NoteActivity.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();
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

    private void getLocation() {
        KeyManager kmlat = new KeyManager("latIV");
        KeyManager kmlong = new KeyManager("longIV");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(NoteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (GPSEnable()) {
                    LocationServices.getFusedLocationProviderClient(NoteActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(NoteActivity.this).removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                                        int index = locationResult.getLocations().size() - 1;
                                        latitude = locationResult.getLocations().get(index).getLatitude();
                                        longitude = locationResult.getLocations().get(index).getLongitude();
                                        String latString = latitude + "";
                                        String longString = longitude + "";
                                        String latToStore = null;
                                        String longToStore = null;
                                        try {
                                            latToStore = Base64.encodeToString(kmlat.encrypt(getApplicationContext(), latString.getBytes("UTF-8")), Base64.DEFAULT);
                                            longToStore = Base64.encodeToString(kmlong.encrypt(getApplicationContext(), longString.getBytes("UTF-8")), Base64.DEFAULT);
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchPaddingException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchProviderException e) {
                                            e.printStackTrace();
                                        } catch (BadPaddingException e) {
                                            e.printStackTrace();
                                        } catch (IllegalBlockSizeException e) {
                                            e.printStackTrace();
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
                                        SharedPreferences.Editor myEdit = sh.edit();
                                        myEdit.putString("longitude", longToStore);
                                        myEdit.putString("latitude", latToStore);
                                        myEdit.apply();
                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    RequestTurnOnGPS();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
}