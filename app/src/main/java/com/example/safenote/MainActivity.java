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

import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText passwordInput= findViewById(R.id.editTextTextPassword);//用户输密码
        Button login =  findViewById(R.id.LoginButton);
        Context context = getApplicationContext();
        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {// TODO: Check password correspondence
                byte[] decodedStoredPH = null;
                byte[] decryptedstoredPH = null;
                KeyManager km = new KeyManager();
                SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);//store password in shared preference
                String storedPasswordHash = sh.getString("password", "");
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
                    if (Arrays.equals(hashPassword(passwordInput.getText().toString()),decryptedstoredPH) || storedPasswordHash.equals("")) {
                        Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                        startActivity(intent);
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        });
        Button setPassword = findViewById(R.id.SetPasswordButton);
        setPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {// TODO: Check password correspondence
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