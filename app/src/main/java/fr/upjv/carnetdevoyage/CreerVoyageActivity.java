package fr.upjv.carnetdevoyage;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import fr.upjv.carnetdevoyage.Model.Voyage;
import fr.upjv.carnetdevoyage.Repository.FirebaseRepository;
import fr.upjv.carnetdevoyage.Service.LocationService;

public class CreerVoyageActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    EditText nomEditText;
    EditText descriptionEditText;
    SeekBar seekBar;
    TextView frequenceTextView;
    FirebaseRepository db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_creer_voyage);
        nomEditText = findViewById(R.id.nomEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        seekBar = findViewById(R.id.seekBar);
        frequenceTextView = findViewById(R.id.frequenceTextVIew);
        seekBar.setMax(60);
        seekBar.setMin(1);
        seekBar.setProgress(15);
        frequenceTextView.setText("Frequence: "+ seekBar.getProgress() +" secondes.");
        db = new FirebaseRepository();
        checkAndRequestLocationPermissions();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    frequenceTextView.setText("Frequence: "+ progress +" secondes.");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }


    public void onClickCreerVoyage(View view) {
        //Verifier si l'autorisation esr accordée
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            Toast.makeText(this, "Vous devez accepter la permission de localisation pour créer un voyage", Toast.LENGTH_LONG).show();

            return;
        }
        String nom = nomEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        int seekbar = seekBar.getProgress();
        if(TextUtils.isEmpty(nom)){
            nomEditText.setError("Le nom est obligatoire");
            nomEditText.requestFocus();
            return;
        }
        Voyage v = new Voyage(nom, description, seekbar);
        db.ajouterVoyage(v).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Voyage créée avec succès", Toast.LENGTH_SHORT).show();
            //Si tout va bien, lancer le service
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("voyageId", documentReference.getId());
            serviceIntent.putExtra("frequence", seekbar);
            startService(serviceIntent);
            finish();
        })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private void checkAndRequestLocationPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
                Toast.makeText(this, "Permission de localisation accordée", Toast.LENGTH_SHORT).show();
            } else {
                // Permission refusée
                Toast.makeText(this, "Permission de localisation refusée - certaines fonctionnalités ne seront pas disponibles", Toast.LENGTH_LONG).show();
            }
        }
    }

}