package com.example.safenote;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetPasswordActivity extends AppCompatActivity {

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button buttonSubmit;

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
                if (newPassword.getText().toString() != confirmPassword.getText().toString()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please make sure you entered new password correctly!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                // TODO: Check password correspondence
                if (oldPassword.getText().toString() == "oldpassword") {
                    // TODO: change password
                    Toast.makeText(
                            getApplicationContext(),
                            "Changed Password successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
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