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
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
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
        String nom = nomEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        int seekbar = seekBar.getProgress();
        if(TextUtils.isEmpty(nom)){
            nomEditText.setError("Le nom est obligatoire");
            nomEditText.requestFocus();
            return;
        }
        //Si la permission de notifications, n'est pas accorder on va la demandé
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        //Si la permission d'acces a la position, n'est pas accorder on va la demandé
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
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
}