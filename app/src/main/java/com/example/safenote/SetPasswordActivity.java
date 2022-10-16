package com.example.safenote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class SetPasswordActivity extends AppCompatActivity {

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button buttonSubmit;

    public static String hashPassword(String password) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.reset();
        md.update(password.getBytes());
        byte[] mdArray = md.digest();
        StringBuilder sb = new StringBuilder(mdArray.length * 2);
        for(byte b : mdArray) {
            int v = b & 0xff;
            if(v < 16)
                sb.append('0');
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        bindViews();
        setButtonSubmit();
    }


    private void bindViews() {
        this.oldPassword = (EditText) findViewById(R.id.editTextTextPassword2);
        this.newPassword = (EditText) findViewById(R.id.editTextTextPassword3);
        this.confirmPassword = (EditText) findViewById(R.id.editTextTextPassword4);
    }

    private void setButtonSubmit() {
        this.buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: add keystore steps here, not storing password in plaintext
                SharedPreferences sh = getSharedPreferences("password", MODE_PRIVATE);//store password in sharedpreference
                String storedPassword = sh.getString("password", "");
                System.out.println("old password is"+storedPassword);
                System.out.println("Entered old password is"+oldPassword.getText().toString());
                if (!newPassword.getText().toString().equals(confirmPassword.getText().toString())) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please make sure you entered new password correctly!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                if (oldPassword.getText().toString().equals(storedPassword) || storedPassword.equals("")) {//old password equals entered old password, or oldpassword is empty (meaning it's new user)
                    System.out.println("entered=stored check passed, old password is" + storedPassword);

                    Toast.makeText(
                            getApplicationContext(),
                            "Changed Password successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                    String toStore = newPassword.getText().toString();//store the password in shared preference as key-value pair
                    String hashed = null;
                    try {
                        hashed = hashPassword(toStore);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor myEdit = sh.edit();
                    myEdit.putString("password", hashed);
                    myEdit.apply();
                    System.out.println("new stored password is " + sh.getString("password", "(failed to get)"));
                } else {
                    System.out.println("Old password entered incorrectly, old password is "+storedPassword);
                    Toast.makeText(
                            getApplicationContext(),
                            "Old password entered incorrectly. Please try again!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
}