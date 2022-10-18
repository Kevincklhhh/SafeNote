package com.example.safenote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SetPasswordActivity extends AppCompatActivity {

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button buttonSubmit;

    public static byte[] hashPassword(String password) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return hash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
            myEdit.putString("password", "");
            myEdit.apply();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        bindViews();
        setButtonSubmit();
    }


    private void bindViews() {
        this.oldPassword = findViewById(R.id.editTextTextPassword2);
        this.newPassword = findViewById(R.id.editTextTextPassword3);
        this.confirmPassword = findViewById(R.id.editTextTextPassword4);
    }

    private void setButtonSubmit() {
        this.buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(view -> {
            byte[] decodedStoredPH = null;
            byte[] decryptedstoredPH = null;
            KeyManager km = new KeyManager();
            SharedPreferences sh = getSharedPreferences("shared_preference", MODE_PRIVATE);//store password in shared preference
            String storedPasswordHash = sh.getString("password", "");
//            SharedPreferences.Editor myEdit = sh.edit();
//            myEdit.putString("password", "");
//            myEdit.apply();
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
//                try {
//                    SharedPreferences.Editor myEdit = sh.edit();
//                    myEdit.putString("password", km.encrypt(getApplicationContext(),"hahaha"));
//                    myEdit.apply();
//                    String storedhahaha = sh.getString("password", "");
//                    System.out.println(storedhahaha);
//                    System.out.println(km.decrypt(getApplicationContext(),storedhahaha));
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

            System.out.println("old password is" + decodedStoredPH);
            System.out.println("Entered old password is" + oldPassword.getText().toString());
            if (!newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                Toast.makeText(
                        getApplicationContext(),
                        "Please make sure you entered new password correctly!",
                        Toast.LENGTH_SHORT
                ).show();
            }
            if (newPassword.getText().length() == 0) {
                Toast.makeText(
                        getApplicationContext(),
                        "Please make sure you entered nonempty password!",
                        Toast.LENGTH_SHORT
                ).show();
            }
            try {
                if (Arrays.equals(hashPassword(oldPassword.getText().toString()), decryptedstoredPH) || storedPasswordHash.equals("")) {//old password equals entered old password, or oldpassword is empty (meaning it's new user)
                    //System.out.println("entered=stored check passed, old password is" + storedPassword);
                    Toast.makeText(
                            getApplicationContext(),
                            "Changed Password successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                    String newPasswordString = newPassword.getText().toString();//store the password in shared preference as key-value pair
                    byte[] hashed = null;
                    String toStore = null;
                    byte[] EncryptedByte = null;
                    try {
                        hashed = hashPassword(newPasswordString);
                        EncryptedByte = km.encrypt(getApplicationContext(), hashed);
                        toStore = Base64.encodeToString(EncryptedByte, Base64.DEFAULT);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchProviderException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor myEdit = sh.edit();
                    myEdit.putString("password", toStore);
                    myEdit.apply();
                    System.out.println("new stored password is " + sh.getString("password", "(failed to get)"));
                } else {
                    System.out.println("Old password entered incorrectly, old password is " + storedPasswordHash);
                    Toast.makeText(
                            getApplicationContext(),
                            "Old password entered incorrectly. Please try again!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
    }
}