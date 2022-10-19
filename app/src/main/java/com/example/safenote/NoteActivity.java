package com.example.safenote;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import androidx.annotation.NonNull;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

public class NoteActivity extends AppCompatActivity {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "Alias";
    public static final String SHARED_PREFENCE = "shared_preference";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private LocationRequest locationRequest;
    private double latitude, longitude;
    EditText note, note_title;
    private SecretKey key;
    private IvParameterSpec ivParameterSpec;
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    public String encrypt(String input, SecretKey key,
                          IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        this.ivParameterSpec = new IvParameterSpec(cipher.getIV());
        byte[] cipherText = cipher.doFinal(input.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        }
        return "";
    }

    public static String decrypt( String cipherText, SecretKey key,
                                 byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] plainText = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
        }
        return new String(plainText);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        note = findViewById(R.id.note);
        note_title = findViewById(R.id.note_title);

        LocationRequest.Builder Build = new LocationRequest.Builder(5000);
        Build.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest = Build.build();
        ivParameterSpec = generateIv();
        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            ks.load(null);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        //SecretKey key = null;
        try {
            if (!ks.containsAlias(KEY_ALIAS)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
                }
            } else {
                try {
                    key = ((KeyStore.SecretKeyEntry) ks.getEntry("Alias", null)).getSecretKey();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        Button addLocation = findViewById(R.id.add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getLocation();
            }
        });

        Button viewLocation = findViewById(R.id.view_location);
        viewLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                KeyManager kmlat = new KeyManager("LatIV");
                KeyManager kmlong = new KeyManager("LongIV");

                String storedLatitude = sh.getString("latitude", "");
                String storedLongitude = sh.getString("longitude", "");
                String decryptedlatitude = null;
                String decryptedlongitude = null;
                SharedPreferences pref = getSharedPreferences(SHARED_PREFENCE, Context.MODE_PRIVATE);
                String latIv = pref.getString("LATITUDEIV", null);
                Cipher latC = null;
                String longIv = pref.getString("LongIV", null);
                Cipher longC = null;
                System.out.println(pref.getAll());
                try {
                    decryptedlatitude = decrypt(storedLatitude,key,ivParameterSpec.getIV());
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
//                byte[] decryptedLong = new byte[0];
//                byte[] decryptedLat = new byte[0];
//                SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
//                try {
//                    byte[] decodedLat = Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT);
//                    byte[] decodedLong = Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT);
//                    latC = Cipher.getInstance(AES_MODE);
//                    try {
//                        latC.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.decode(latIv, Base64.DEFAULT)));
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    decryptedLat = latC.doFinal(Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT));
//                    //decryptedlatitude = Base64.encodeToString(kmlat.decrypt(getApplicationContext(),Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT)), Base64.DEFAULT);
//                    //decryptedlongitude = Base64.encodeToString(kmlong.decrypt(getApplicationContext(),Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT)), Base64.DEFAULT);
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                } catch (NoSuchPaddingException e) {
//                    e.printStackTrace();
//                } catch (BadPaddingException e) {
//                    e.printStackTrace();
//                } catch (IllegalBlockSizeException e) {
//                    e.printStackTrace();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    decryptedLat = Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT);
//                    decryptedLong = Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT);
//                    decryptedlatitude = Base64.encodeToString(kmlat.decrypt(getApplicationContext(),Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT)), Base64.DEFAULT);
//                    decryptedlongitude = Base64.encodeToString(kmlong.decrypt(getApplicationContext(),Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT)), Base64.DEFAULT);
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                } catch (NoSuchPaddingException e) {
//                    e.printStackTrace();
//                } catch (NoSuchProviderException e) {
//                    e.printStackTrace();
//                } catch (BadPaddingException e) {
//                    e.printStackTrace();
//                } catch (IllegalBlockSizeException e) {
//                    e.printStackTrace();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                //System.out.println(decryptedLong);
                //System.out.println(decryptedLat);
                
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
        finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String content = note_title.getText().toString() + "\n" + note.getText().toString();
                StorageToInternalStorage("note.txt", content);
            }
        });

        String content = "\n";
        try {
            content = ReadFromInternalStorage("note.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("content is" + content);
        if (content.length() == 0) content = "\n";
        int separate = content.indexOf("\n");
        note_title.setText(content.substring(0, separate));
        note.setText(content.substring(separate + 1, content.length()));

    }

    private void StorageToInternalStorage(String fileName, String content) {
        File path = getApplicationContext().getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(content.getBytes());
            writer.close();
            Toast.makeText(getApplicationContext(), "Wrote to file: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String ReadFromInternalStorage(String fileName) throws IOException {
        File path = getApplicationContext().getFilesDir();
        File place = new File(path, fileName);
        byte[] content = new byte[(int) place.length()];
        if (!place.isFile() && !place.createNewFile()) {
            throw new IOException("Error creating new file: " + place.getAbsolutePath());
        }
        try {
            FileInputStream reader = new FileInputStream(place);
            reader.read(content);
            return new String(content);
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
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
        KeyManager kmlat = new KeyManager("LatIV");
        KeyManager kmlong = new KeyManager("LongIV");
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
                                        String latString = latitude+"";
                                        String longString = latitude+"";
                                        String latToStore = null;
                                        String longToStore = null;
                                        byte[] encryptedBytes = new byte[0];
                                        SharedPreferences pref = getSharedPreferences("shared_preference", Context.MODE_PRIVATE);
                                        String LATiv;
//                                        try {
//                                            Cipher encryptC = Cipher.getInstance(AES_MODE);
//                                            try {
//                                                encryptC.init(Cipher.ENCRYPT_MODE, key);
//                                                LATiv = Base64.encodeToString(encryptC.getIV(), Base64.DEFAULT);
//                                                SharedPreferences.Editor edit = pref.edit();
//                                                edit.putString("LATITUDEIV", LATiv);
//                                                edit.apply();
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                            byte[] encodedBytes = latString.getBytes("UTF-8");
//                                            encryptedBytes = encryptC.doFinal(latString.getBytes("UTF-8"));
//                                        } catch (NoSuchAlgorithmException e) {
//                                            e.printStackTrace();
//                                        } catch (NoSuchPaddingException | UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
//                                            e.printStackTrace();
//                                        }
//
                                        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);

                                        SharedPreferences.Editor myEdit = sh.edit();
                                        //myEdit.putString("longitude", longToStore);
                                        //latToStore = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

                                        try {
                                            latToStore=encrypt(latString,key,ivParameterSpec);
                                        } catch (NoSuchPaddingException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (InvalidAlgorithmParameterException e) {
                                            e.printStackTrace();
                                        } catch (InvalidKeyException e) {
                                            e.printStackTrace();
                                        } catch (BadPaddingException e) {
                                            e.printStackTrace();
                                        } catch (IllegalBlockSizeException e) {
                                            e.printStackTrace();
                                        }
                                        myEdit.putString("latitude", latToStore);
                                        myEdit.apply();
                                        //System.out.println("stored"+longToStore);
                                        System.out.println("stored"+latToStore);
                                        System.out.println(sh.getAll());

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