package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.carnetdevoyage.Model.Voyage;
import fr.upjv.carnetdevoyage.Repository.FirebaseRepository;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRepository db;
    private FirebaseAuth auth;
    private List<Voyage> voyages;
    private VoyageAdapter monVoyageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voyages = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        monVoyageAdapter = new VoyageAdapter(voyages);
        recyclerView.setAdapter(monVoyageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = new FirebaseRepository();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }

        // Clique sur un voyage
        monVoyageAdapter.setOnItemClickListener(new VoyageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Voyage voyage) {
                Intent i = new Intent(MainActivity.this, DetailVoyageActivity.class);
                i.putExtra("voyageId",voyage.getIdVoyage());
                startActivity(i);
            }
        });

        loadVoyages();
    }

    private void loadVoyages() {
        this.voyages = new ArrayList<>();

        db.recupererTousVoyages().addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("MainActivity", "Erreur Firestore", error);
                Toast.makeText(MainActivity.this, "Erreur de chargement: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (value != null) {
                voyages.clear(); // Nettoie la liste avant d'ajouter

                for (QueryDocumentSnapshot document : value) {
                    Voyage voyage = document.toObject(Voyage.class);
                    if (voyage != null) {
                        voyage.setIdVoyage(document.getId());
                        voyages.add(voyage);
                    } else {
                        Log.w("MainActivity", "Document non convertible : " + document.getId());
                    }
                }

                monVoyageAdapter.setLesVoyages(voyages);

                if (voyages.isEmpty()) {
                    Toast.makeText(this, "Aucun voyage n'est récupéré", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.w("MainActivity", "Valeur nulle, mais sans erreur.");
            }
        });
    }

    public void onClickCreerVoyage(View view) {
        Intent intent = new Intent(this, CreerVoyageActivity.class);
        startActivity(intent);
    }
}
