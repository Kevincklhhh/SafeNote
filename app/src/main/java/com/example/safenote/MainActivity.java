package com.example.safenote;

import static com.example.safenote.SetPasswordActivity.hashPassword;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private static final String KEY_ALIAS = "Alias";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText passwordInput = findViewById(R.id.editTextTextPassword);
        Button login = findViewById(R.id.LoginButton);
        Context context = getApplicationContext();
        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] decodedStoredPH = null;
                byte[] decryptedstoredPH = null;
                KeyManager km = new KeyManager("passwordIV");
                SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);//store password in shared preference
                String storedPasswordHash = sh.getString("password", "");
                if (storedPasswordHash.equals("")) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please set a password before first use!",
                            Toast.LENGTH_SHORT
                    ).show();
                    System.out.println("Please set a password!");
                } else if(passwordInput.getText().toString().equals("")){
                    Toast.makeText(
                            getApplicationContext(),
                            "Please enter a password!",
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
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SetPasswordActivity.class);
                startActivity(intent);
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