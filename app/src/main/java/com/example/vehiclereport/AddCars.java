package com.example.vehiclereport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class AddCars extends AppCompatActivity {

    private Button btnRegisterCar;
    private EditText edModel, edNumberPlate, edYearRegister, edKms, edInsurance, edITV;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cars);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        btnRegisterCar = findViewById(R.id.btnRegisterCar);
        edModel = findViewById(R.id.edModel);
        edNumberPlate = findViewById(R.id.edNumberPlate);
        edYearRegister = findViewById(R.id.edRegistration);
        edKms = findViewById(R.id.edKms);
        edInsurance = findViewById(R.id.edInsurance);
        edITV = findViewById(R.id.edITV);

        selectDate();

        btnRegisterCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getModel = edModel.getText().toString();
                String getNumberPlate = edNumberPlate.getText().toString();
                String getYearRegister = edYearRegister.getText().toString();
                String getKms = edKms.getText().toString();
                String getInsurance = edInsurance.getText().toString();
                String getITV = edITV.getText().toString();

                if(TextUtils.isEmpty(getModel) || TextUtils.isEmpty(getNumberPlate) ||
                    TextUtils.isEmpty(getYearRegister) || TextUtils.isEmpty(getKms) ||
                    TextUtils.isEmpty(getInsurance) || TextUtils.isEmpty(getITV)){
                    Toast.makeText(AddCars.this,"Verifique que todos los campos están rellenos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Pattern.matches(".*[.#$\\[\\]].*", getModel)) {
                    Toast.makeText(AddCars.this, "El modelo no puede tener caracteres especiales", Toast.LENGTH_SHORT).show();
                    return;
                }
                Query query = databaseReference.child(uid).child("Cars").orderByChild("Modelo").equalTo(getModel);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Toast.makeText(AddCars.this,"Ya existe un vehículo con ese modelo", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("Modelo",getModel);
                            hashMap.put("Numero de Matricula", getNumberPlate);
                            hashMap.put("Fecha matriculacion", getYearRegister);
                            hashMap.put("Kilometros actuales", getKms);
                            hashMap.put("Fecha seguro coche",getInsurance);
                            hashMap.put("Fecha ultima ITV", getITV);
                            databaseReference.child(uid).child("Cars").child(getModel).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(AddCars.this, "Coche registrado correctamente", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), Home.class));
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddCars.this, "Hubo un error en la BD", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void selectDate(){
        DatePickerDialog.OnDateSetListener dateYearRegister = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                edYearRegister.setText(updateDate());
            }
        };

        DatePickerDialog.OnDateSetListener dateInsurance = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                edInsurance.setText(updateDate());
            }
        };

        DatePickerDialog.OnDateSetListener dateITV = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                edITV.setText(updateDate());
            }
        };

        edYearRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddCars.this, dateYearRegister, calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        edInsurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddCars.this, dateInsurance, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        edITV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddCars.this, dateITV, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @NonNull
    private String updateDate(){
        String Format = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(Format,Locale.US);
        return dateFormat.format(calendar.getTime());
    }

}