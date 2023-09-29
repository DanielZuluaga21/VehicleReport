package com.example.vehiclereport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.Serializable;
import java.util.Map;

public class ControlCar extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView txtModel, txtNumberPlate, txtRegistration, txtKms, txtInsurance, txtITV, txtTire;
    private String modelBD, numberPlateBD, registrationBD, kmsBD, insuranceBD, itvBD;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_car);

        bottomNavigationView = findViewById(R.id.bottom_navigator);

        //A través del Intent se recupera la variable selectedCarBD declarada en Home.class
        Intent intent = getIntent();
        Map<String, Object> selectedCarBD = (Map<String, Object>) intent.getSerializableExtra("selectedCarDetails");

        try {
            txtModel = findViewById(R.id.txtModel);
            txtNumberPlate = findViewById(R.id.txtNumberPlate);
            txtRegistration = findViewById(R.id.txtRegistration);
            txtKms = findViewById(R.id.txtKms);
            txtInsurance = findViewById(R.id.txtInsurance);
            txtITV = findViewById(R.id.txtITV);
            txtTire = findViewById(R.id.txtTire);

            modelBD = selectedCarBD.get("Modelo").toString();
            numberPlateBD = selectedCarBD.get("Numero de Matricula").toString();
            registrationBD = selectedCarBD.get("Fecha matriculacion").toString();
            kmsBD = selectedCarBD.get("Kilometros actuales").toString();
            insuranceBD = selectedCarBD.get("Fecha seguro coche").toString();
            itvBD = selectedCarBD.get("Fecha ultima ITV").toString();

            txtModel.setText(modelBD);
            txtNumberPlate.setText(numberPlateBD);
            txtRegistration.setText(registrationBD);
            txtKms.setText(kmsBD);
            txtInsurance.setText(insuranceBD);
            txtITV.setText(itvBD);

            int currentKm = Integer.parseInt(kmsBD);
            checkTire(currentKm);
        }
        catch(Exception e){
            Toast.makeText(ControlCar.this, "Debe seleccionar un vehículo primero", Toast.LENGTH_SHORT).show();
        }


        bottomNavigationView.setSelectedItemId(R.id.controlCar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        intent.putExtra("selectedCarDetails", (Serializable) selectedCarBD);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.controlCar:
                        return true;
                    case R.id.settings:
                        Intent intent1 = new Intent(getApplicationContext(), Settings.class);
                        intent1.putExtra("selectedCarDetails", (Serializable) selectedCarBD);
                        startActivity(intent1);
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    public void checkTire(int currentkm) {
        int currentKms = currentkm;

        if (currentKms == 0) {
            txtTire.setText("Bueno");
            txtTire.setTextAppearance(this, R.style.txtGoodStatus);
        } else {
            int tireWear = (45000 - (currentKms % 45000)) % 45000;

            if (tireWear == 0) {
                txtTire.setText("Defectuoso");
                txtTire.setTextAppearance(this, R.style.txtBadStatus);
            } else if (tireWear <= 22500) {
                txtTire.setText("Medio");
                txtTire.setTextAppearance(this, R.style.txtMediumStatus);
            } else {
                txtTire.setText("Bueno");
                txtTire.setTextAppearance(this, R.style.txtGoodStatus);
            }
        }
    }



}