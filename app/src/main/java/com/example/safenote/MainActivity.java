package com.example.safenote;

import static com.example.safenote.SetPasswordActivity.hashPassword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private static final String KEY_ALIAS = "Alias";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText passwordInput = findViewById(R.id.editTextTextPassword);//用户输密码
        Button login = findViewById(R.id.LoginButton);
        Context context = getApplicationContext();
        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
        startActivity(intent);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {// TODO: Check password correspondence
                byte[] decodedStoredPH = null;
                byte[] decryptedstoredPH = null;
                KeyManager km = new KeyManager("passwordIV");
                SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);//store password in shared preference
                String storedPasswordHash = sh.getString("password", "");
                if (storedPasswordHash.equals("")) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please set a password!",
                            Toast.LENGTH_SHORT
                    ).show();
                    System.out.println("Please set a password!");
                } else if(passwordInput.getText().toString().equals("")){
                    Toast.makeText(
                            getApplicationContext(),
                            "Please enter a non-empty password!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                else {
                    try {
                        decodedStoredPH = Base64.decode(storedPasswordHash.getBytes("UTF-8"), Base64.DEFAULT);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        decryptedstoredPH = km.decrypt(getApplicationContext(), decodedStoredPH);
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
                    try {
                        if (Arrays.equals(hashPassword(passwordInput.getText().toString()), decryptedstoredPH)) {
                            System.out.println("hash compare passed");
                            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                            startActivity(intent);
                        }else{
                            System.out.println("wrong password");
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Wrong Password!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        Button setPassword = findViewById(R.id.SetPasswordButton);
        setPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {// TODO: testing

                String storedLatitude = sh.getString("latitude", "");
                String storedLongitude = sh.getString("longitude", "");
                String decryptedlatitude = null;
                String decryptedlongitude = null;



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
                SecretKey key = null;
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
                //key = keyGenerator.generateKey();
                SharedPreferences pref = getSharedPreferences("shared_preference", Context.MODE_PRIVATE);
                String latString = "100";
                String iv;
                byte[] encryptedBytes = new byte[0];
                try {
                    Cipher encryptC = Cipher.getInstance(AES_MODE);
                    try {
                        encryptC.init(Cipher.ENCRYPT_MODE, key);
                        iv = Base64.encodeToString(encryptC.getIV(), Base64.DEFAULT);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("testIV", iv);
                        edit.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    byte[] encodedBytes = latString.getBytes("UTF-8");
                    encryptedBytes = encryptC.doFinal(latString.getBytes("UTF-8"));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException | UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                String latIv = pref.getString("latIV", null);
                String testIv = pref.getString("testIV", null);
                Cipher latC = null;
                String longIv = pref.getString("longIV", null);
                Cipher longC = null;
                System.out.println(pref.getAll());
                byte[] decryptedLong = new byte[0];
                byte[] decryptedLat = new byte[0];

                try {
                    byte[] decodedLat = Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT);
                    byte[] decodedLong = Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT);
                    latC = Cipher.getInstance(AES_MODE);
                    try {
                        latC.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.decode(testIv, Base64.DEFAULT)));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    decryptedLat = latC.doFinal(encryptedBytes);
                    //decryptedlatitude = Base64.encodeToString(kmlat.decrypt(getApplicationContext(),Base64.decode(storedLatitude.getBytes("UTF-8"), Base64.DEFAULT)), Base64.DEFAULT);
                    //decryptedlongitude = Base64.encodeToString(kmlong.decrypt(getApplicationContext(),Base64.decode(storedLongitude.getBytes("UTF-8"), Base64.DEFAULT)), Base64.DEFAULT);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //System.out.println(decryptedLong);
                //System.out.println(decryptedLat);

                double latitude = Double.parseDouble(decryptedlatitude);
                double longitude = Double.parseDouble(decryptedlongitude);


//                System.out.println(sh.getAll());
//                Intent intent = new Intent(getApplicationContext(), SetPasswordActivity.class);
//                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}