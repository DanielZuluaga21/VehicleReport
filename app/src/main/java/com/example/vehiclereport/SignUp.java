package com.example.vehiclereport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    private EditText edUsername, edPassword, edConfirmPassword;
    private Button btnCreateUser, btnBack;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edUsername = findViewById(R.id.edUsername);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        mAuth = FirebaseAuth.getInstance();

        btnCreateUser.setOnClickListener(this);
        btnBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCreateUser:
                createUser();
                break;
            case R.id.btnBack:
                goBack();
                break;
        }
    }


    public void createUser(){
        progressBar.setVisibility(View.VISIBLE);
        String user,password, confirmPass;
        user = String.valueOf(edUsername.getText()).trim();
        password = String.valueOf(edPassword.getText()).trim();
        confirmPass = String.valueOf(edConfirmPassword.getText()).trim();

        if(TextUtils.isEmpty(user)){
            Toast.makeText(SignUp.this, "Introduzca un email", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(SignUp.this, "Introduzca una contraseña", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if(password.length() < 8){
            Toast.makeText(SignUp.this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if(!password.equals(confirmPass)){
            Toast.makeText(SignUp.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.createUserWithEmailAndPassword(user, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(SignUp.this, "Cuenta creada correctamente.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUp.this, "No se pudo crear la cuenta.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public void goBack(){
        Intent intent = new Intent(getApplicationContext(),Login.class);
        startActivity(intent);
        finish();
    }
}