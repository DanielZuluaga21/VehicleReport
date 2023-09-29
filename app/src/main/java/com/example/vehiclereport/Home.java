package com.example.vehiclereport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Home extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CardView cardRegister, cardModified, cardSelected, cardDistance;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    final Calendar calendar = Calendar.getInstance();
    private Map<String, Object> selectedCarBD;
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        cardRegister = findViewById(R.id.cardRegister);
        cardModified = findViewById(R.id.cardModified);
        cardSelected = findViewById(R.id.cardSelected);
        cardDistance = findViewById(R.id.cardDistance);



        //CardView para registrar el coche
        cardRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddCars.class));
            }
        });

        //CardView que introduce kms del coche seleccionado.
        cardDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog modal = new Dialog(Home.this);
                modal.setContentView(R.layout.activity_km);

                EditText edKms = modal.findViewById(R.id.edKms);

                modal.show();

                Button btnRegisterKms = modal.findViewById(R.id.btnRegisterKms);
                btnRegisterKms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String newKms = edKms.getText().toString();
                            if (TextUtils.isEmpty(newKms)) {
                                Toast.makeText(Home.this, "Por favor, introduzca los kms semanales", Toast.LENGTH_SHORT).show();
                            } else {
                                int kmsToAdd = Integer.parseInt(newKms);
                                //Permite recuperar los kilometros de la BD.
                                int kmsBD = Integer.parseInt(selectedCarBD.get("Kilometros actuales").toString());
                                int totalKms = kmsBD + kmsToAdd;

                                //Se añaden los kms introducidos al total.
                                selectedCarBD.put("Kilometros actuales", totalKms);

                                //Se recupera la jerarquía en la que se divide la BD.
                                String modelBD = selectedCarBD.get("Modelo").toString();
                                /*
                                * Jerarquia de la BD -- UID (usuario) --> Cars --> Modelo --> Datos vehículos
                                * Se modifica el valor de los kms
                                */
                                databaseReference.child(uid).child("Cars").child(modelBD).setValue(selectedCarBD);

                                Toast.makeText(Home.this, "Kilómetros añadidos correctamente", Toast.LENGTH_SHORT).show();
                                modal.dismiss();
                            }
                        }
                        catch (Exception e){
                            Toast.makeText(Home.this, "Debe seleccionar un vehiculo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

        //CardView que selecciona el coche.
        cardSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se crea el diálogo personalizado
                firebaseDatabase = FirebaseDatabase.getInstance();
                databaseReference = firebaseDatabase.getReference();

                Dialog dialog = new Dialog(Home.this);
                dialog.setContentView(R.layout.activity_selected_cars);

                // Se obtiene la referencia del Spinner
                Spinner spListCars = dialog.findViewById(R.id.spListCars);



                databaseReference.child(uid).child("Cars").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> carList = new ArrayList<>();
                        for (DataSnapshot carSnapshot : dataSnapshot.getChildren()) {
                            String modelo = carSnapshot.child("Modelo").getValue(String.class);
                            carList.add(modelo);
                        }

                        // Crear un ArrayAdapter con la lista de coches
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Home.this,
                                android.R.layout.simple_spinner_dropdown_item, carList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Configurar el adaptador en el Spinner
                        spListCars.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Home.this, "No se ha podido recuperar los datos de la BD", Toast.LENGTH_SHORT).show();
                    }
                });

                Button btnAccept = dialog.findViewById(R.id.btnAccept);


                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Obtener el coche seleccionado del Spinner
                        try{
                            String selectedCar = spListCars.getSelectedItem().toString();
                            databaseReference.child(uid).child("Cars").orderByChild("Modelo").equalTo(selectedCar).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        DataSnapshot carData = snapshot.getChildren().iterator().next();
                                        selectedCarBD = carData.getValue(new GenericTypeIndicator<Map<String, Object>>() {});
                                        Toast.makeText(Home.this, "Coche seleccionado correctamente", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Home.this, "No se ha podido recuperar los datos de la BD", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        catch(Exception e){
                            Toast.makeText(Home.this, "Debe registrar un vehículo", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        //Cardview que permite la modificacion de un vehiculo.
        cardModified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCarBD != null) {
                    Dialog modifyDialog = new Dialog(Home.this);
                    modifyDialog.setContentView(R.layout.activity_modify_car);

                    //Se obtienen la referencia de los ID de los EditText
                    EditText edChangeModel = modifyDialog.findViewById(R.id.edChangeModel);
                    EditText edChangeNumberPlate = modifyDialog.findViewById(R.id.edChangeNumberPlate);
                    EditText edChangeRegistration = modifyDialog.findViewById(R.id.edChangeRegistration);
                    EditText edChangeKms = modifyDialog.findViewById(R.id.edChangeKms);
                    EditText edChangeInsurance = modifyDialog.findViewById(R.id.edChangeInsurance);
                    EditText edChangeITV = modifyDialog.findViewById(R.id.edChangeITV);

                    // Se muestran los datos de la BD
                    edChangeModel.setText(selectedCarBD.get("Modelo").toString());
                    edChangeNumberPlate.setText(selectedCarBD.get("Numero de Matricula").toString());
                    edChangeRegistration.setText(selectedCarBD.get("Fecha matriculacion").toString());
                    edChangeKms.setText(selectedCarBD.get("Kilometros actuales").toString());
                    edChangeInsurance.setText(selectedCarBD.get("Fecha seguro coche").toString());
                    edChangeITV.setText(selectedCarBD.get("Fecha ultima ITV").toString());

                    edChangeRegistration.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    edChangeRegistration.setText(updateDate());
                                }
                            };

                            new DatePickerDialog(Home.this, dateSetListener, calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                        }
                    });


                    edChangeInsurance.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    edChangeInsurance.setText(updateDate());
                                }
                            };

                            new DatePickerDialog(Home.this, dateSetListener, calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                        }
                    });


                    edChangeITV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    edChangeITV.setText(updateDate());
                                }
                            };
                            new DatePickerDialog(Home.this, dateSetListener, calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                        }
                    });


                    // Implementacion del boton para modificar el vehiculo.
                    Button btnModify = modifyDialog.findViewById(R.id.btnModify);
                    btnModify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Se obtienen los valores modificados de los campos EditText
                            String modifiedNumberPlate = edChangeNumberPlate.getText().toString();
                            String modifiedRegistration = edChangeRegistration.getText().toString();
                            String modifiedKms = edChangeKms.getText().toString();
                            String modifiedInsurance = edChangeInsurance.getText().toString();
                            String modifiedITV = edChangeITV.getText().toString();


                            // Se asignan las modificaciones en la BD
                            selectedCarBD.put("Numero de Matricula", modifiedNumberPlate);
                            selectedCarBD.put("Fecha matriculacion", modifiedRegistration);
                            selectedCarBD.put("Kilometros actuales", modifiedKms);
                            selectedCarBD.put("Fecha seguro coche", modifiedInsurance);
                            selectedCarBD.put("Fecha ultima ITV", modifiedITV);


                            // Se actualizan los datos en la BD.
                            String modelBD = selectedCarBD.get("Modelo").toString();
                            databaseReference.child(uid).child("Cars").child(modelBD).setValue(selectedCarBD);

                            Toast.makeText(Home.this, "Datos modificados correctamente.", Toast.LENGTH_SHORT).show();

                            modifyDialog.dismiss();
                        }
                    });

                    modifyDialog.show();
                }
                else {
                    Toast.makeText(Home.this, "Seleccione un coche para modificarlo.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        //Interactua con el boton Inicio, Ver y Ajustes.
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        return true;
                    case R.id.controlCar:
                        Intent intent = new Intent(getApplicationContext(), ControlCar.class);
                        intent.putExtra("selectedCarDetails", (Serializable)selectedCarBD);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.settings:
                        Intent intent1 = new Intent(getApplicationContext(), Settings.class);
                        intent1.putExtra("selectedCarDetails", (Serializable)selectedCarBD);
                        startActivity(intent1);
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    @NonNull
    private String updateDate() {
        String Format = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(Format, Locale.US);
        return dateFormat.format(calendar.getTime());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        selectedCarBD = null;
    }

}
