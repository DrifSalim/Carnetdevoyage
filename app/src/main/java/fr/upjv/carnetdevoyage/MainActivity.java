package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    FirebaseRepository db;
    FirebaseAuth auth;
    private List<Voyage> voyages;
    VoyageAdapter monVoyageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        voyages = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        monVoyageAdapter=new VoyageAdapter(voyages);
        recyclerView.setAdapter(monVoyageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = new FirebaseRepository();
        auth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null ){
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }
        loadVoyages();

    }
    private void loadVoyages() {
        this.voyages=new ArrayList<>();
        db.recupererTousVoyages().addSnapshotListener((value, error) -> {
            if (error != null) {
                // Gérer l'erreur

                Log.e("MainActivity", "Erreur au démarrage du SnapshotListener", error);


                Toast.makeText(MainActivity.this, "Erreur de chargement: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (value != null) {
                for (QueryDocumentSnapshot document : value) {
                    Voyage voyage = document.toObject(Voyage.class);

                    if (voyage != null) {
                        voyage.setIdVoyage(document.getId());
                        voyages.add(voyage);
                    } else {
                        Log.w("MainActivity", "Document Firestore ne peut pas être converti en Voyage: " + document.getId());
                    }
                }

                monVoyageAdapter.setLesVoyages(voyages);

                if (voyages.isEmpty()) {
                    Toast.makeText(this,"Aucun voyage n'est récupéré",Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this,""+voyages.toString(),Toast.LENGTH_LONG).show();


                }
            } else {
                Log.w("MainActivity", "Value est null dans SnapshotListener, mais error est aussi null.");
            }});


    }

    public void onClickCreerVoyage(View view) {
        Intent intent = new Intent(this, CreerVoyageActivity.class);
        startActivity(intent);
    }

}
