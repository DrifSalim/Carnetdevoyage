package fr.upjv.carnetdevoyage;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class CreerVoyageActivity extends AppCompatActivity {
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
            return;
        }
        Voyage v = new Voyage(nom, description, seekbar);
        db.ajouterVoyage(v).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Voyage ajouté avec succès", Toast.LENGTH_SHORT).show();

        })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });





    }
}