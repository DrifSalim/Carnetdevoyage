package fr.upjv.carnetdevoyage;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class DetailVoyageActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseRepository db;
    private Voyage currentVoyage;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView textViewNom, textViewDescription, textViewDateDebut;
    private TextView textViewStatus;
    private MapView mapView;
    private boolean isEncours=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_voyage);
        db = new FirebaseRepository();
        //fusedLocationProvider pour recuperer la dernière position connu de l'utilisateur
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    public void onClickEnregistrer(View view) {
    }

    public void onClickTerminerVoyage(View view) {
    }

    public void onClickExporter(View view) {
    }
}