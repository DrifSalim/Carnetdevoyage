package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.Manifest;

public class DetailVoyageActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseRepository db;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Voyage currentVoyage;
    private String voyageId;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView textViewNom, textViewDescription, textViewDateDebut;
    private TextView textViewStatus;
    private MapView mapView;
    private boolean isEncours=false;
    private Button buttonEnregistrerPosition, buttonArreterVoyage, buttonExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_voyage);
        db = new FirebaseRepository();
        //fusedLocationProvider pour recuperer la dernière position connu de l'utilisateur
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        voyageId = getIntent().getStringExtra("voyageId");
        if (voyageId == null) {
            Toast.makeText(this, "Erreur: ID de voyage invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        textViewNom = findViewById(R.id.textViewNom);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewDateDebut = findViewById(R.id.textViewDateDebut);
        textViewStatus = findViewById(R.id.textViewStatut);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        buttonArreterVoyage = findViewById(R.id.buttonArreterVoyage);
        buttonEnregistrerPosition = findViewById(R.id.buttonEnregistrerPosition);
        buttonExport = findViewById(R.id.buttonExport);

        loadVoyage();
    }
    private void loadVoyage() {
        //Charger d'abord les informations du voyage
        db.recupererVoyage(voyageId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Voyage voyage = documentSnapshot.toObject(Voyage.class);
                        if (voyage != null) {
                            voyage.setIdVoyage(documentSnapshot.getId());

                            //Afficher les informations du voyage dans l'interface
                            afficherInfoVoyage(voyage);

                            //les points du voyage
                            loadPointsForVoyage();
                        } else {
                            Toast.makeText(this, "Erreur lors du chargement du voyage", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Voyage introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailVoyage", "Erreur lors du chargement du voyage", e);
                    Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                });
    }
    private void afficherInfoVoyage(Voyage voyage) {
        textViewNom.setText(voyage.getNom());
        textViewDescription.setText(voyage.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateDebut = dateFormat.format(voyage.getDebut().toDate());
        textViewDateDebut.setText("Débuté le " + dateDebut);

        if (voyage.isEncours()) {
            textViewStatus.setText("En cours");
            textViewStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            buttonEnregistrerPosition.setVisibility(View.VISIBLE);
            buttonArreterVoyage.setVisibility(View.VISIBLE);

        } else {
            textViewStatus.setText("Terminé");
            textViewStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            buttonEnregistrerPosition.setVisibility(View.GONE);
            buttonArreterVoyage.setVisibility(View.GONE);

            if (voyage.getFin() != null) {
                String dateFin = dateFormat.format(voyage.getFin().toDate());
                textViewDateDebut.setText(textViewDateDebut.getText() + "\nTerminé le " + dateFin);
            }
        }
    }
    private void loadPointsForVoyage() {
        db.recupererPointsDuVoyage(voyageId)
                .addOnSuccessListener(querySnapshot -> {
                    List<Point> points = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Point point = doc.toObject(Point.class);
                        if (point != null) {
                            point.setIdPoint(doc.getId());
                            points.add(point);
                        }
                    }

                    // Afficher les points sur la carte
                    displayPointsOnMap(points);

                    Log.d("DetailVoyage", "Points chargés: " + points.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailVoyage", "Erreur lors du chargement des points", e);
                    Toast.makeText(this, "Erreur lors du chargement des points", Toast.LENGTH_SHORT).show();
                });
    }
    private void displayPointsOnMap(List<Point> points) {
        if (map == null) {
            Log.e("Map", "Map n'est pas prête pour mettre à jour le chemin du voyage.");
            return;
        }

        map.clear();

        if (points == null || points.isEmpty()) {
            Toast.makeText(this, "Aucun point enregistré pour ce voyage.", Toast.LENGTH_SHORT).show();
            centerMapOnUserLocationOrDefault();
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        //Recuperer les points pour dessiner le chemin
        for (Point point : points) {
            LatLng position = new LatLng(point.getLatitude(), point.getLongitude());
            polylineOptions.add(position);
            boundsBuilder.include(position);
        }

        polylineOptions.color(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        polylineOptions.width(8);

        map.addPolyline(polylineOptions);

        Point debut = points.get(0);
        Point fin = points.get(points.size() - 1);

        map.addMarker(new MarkerOptions()
                .position(new LatLng(debut.getLatitude(), fin.getLongitude()))
                .title("Départ"));

        String endMarkerTitle = currentVoyage.isEncours() ? "Position actuelle" : "Arrivée";
        map.addMarker(new MarkerOptions()
                .position(new LatLng(fin.getLatitude(), fin.getLongitude()))
                .title(endMarkerTitle));

        if (points.size() > 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(fin.getLatitude(), fin.getLongitude()), 15));
        }
    }

    private void centerMapOnUserLocationOrDefault() {
        if (map == null) {
            Log.e("Map", "Map n'est pas prête pour centrer sur la position de l'utilisateur.");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        } else {
                            Log.w("Map", "Position non disponible. Centrage sur une position par défaut.");
                            LatLng defaultLocation = new LatLng(48.8566, 2.3522); // Paris
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Map", "Erreur lors de la récupération de la position de l'utilisateur.", e);
                        LatLng defaultLocation = new LatLng(48.8566, 2.3522); // Paris
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
                    });
        } else {
            Log.d("Permission", "Demande de permission de localisation.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            LatLng defaultLocation = new LatLng(48.8566, 2.3522); // Paris
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si la permission est accordée, réessayez de centrer la carte ou d'enregistrer la position
                if (currentVoyage != null && currentVoyage.getPosition() != null && !currentVoyage.getPosition().isEmpty()) {
                    updateMapWithVoyagePath(currentVoyage.getPosition());
                } else {
                    centerMapOnUserLocationOrDefault();
                }
                // Si l'utilisateur a cliqué sur "Enregistrer une position" et que la permission est accordée,
                // vous pourriez vouloir appeler onClickEnregistrer() ici si c'était le cas d'origine.
                // Pour l'instant, on se contente de rafraîchir la carte.
            } else {
                Toast.makeText(this, "Permission de localisation requise pour certaines fonctionnalités.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (currentVoyage != null && currentVoyage.getPosition() != null && !currentVoyage.getPosition().isEmpty()) {
            updateMapWithVoyagePath(currentVoyage.getPosition());
        } else {
            centerMapOnUserLocationOrDefault();
        }
    }
    private void updateMapWithVoyagePath(List<Point> points) {
        if (map == null) {
            Log.e("Map", "Map n'est pas prête pour mettre à jour le chemin du voyage.");
            return;
        }

        map.clear();

        if (points == null || points.isEmpty()) {
            Toast.makeText(this, "Aucun point enregistré pour ce voyage.", Toast.LENGTH_SHORT).show();
            centerMapOnUserLocationOrDefault();
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (Point point : points) {
            LatLng position = new LatLng(point.getLatitude(), point.getLongitude());
            polylineOptions.add(position);
            boundsBuilder.include(position);
        }

        polylineOptions.color(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        polylineOptions.width(8);

        map.addPolyline(polylineOptions);

        Point start = points.get(0);
        Point end = points.get(points.size() - 1);

        map.addMarker(new MarkerOptions()
                .position(new LatLng(start.getLatitude(), start.getLongitude()))
                .title("Départ"));

        String endMarkerTitle = currentVoyage.isEncours() ? "Position actuelle" : "Arrivée";
        map.addMarker(new MarkerOptions()
                .position(new LatLng(end.getLatitude(), end.getLongitude()))
                .title(endMarkerTitle));

        if (points.size() > 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(end.getLatitude(), end.getLongitude()), 15));
        }
    }

    public void onClickTerminerVoyage(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Terminer le voyage")
                .setMessage("Êtes-vous sûr de vouloir terminer ce voyage ?")
                .setPositiveButton("Oui", (dialog, which) -> terminerVoyage())
                .setNegativeButton("Non", null)
                .show();
    }
    public void onClickEnregistrer(View view) {

    }


    public void onClickExporter(View view) {
    }
    private void terminerVoyage() {
        if (currentVoyage == null || !currentVoyage.isEncours()) {
            return;
        }

        // Arrêter le service de suivi si actif lors de la terminaison du voyage
        if (isEncours) {
            stopTracking();
        }

        currentVoyage.terminerVoyage();
        db.updateVoyage(currentVoyage)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DetailVoyageActivity.this, "Voyage terminé", Toast.LENGTH_SHORT).show();
                    afficherInfoVoyage(currentVoyage); // Mettre à jour l'interface utilisateur
                    loadVoyage();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailVoyageActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void stopTracking() {
        if (!isEncours) {
            return;
        }

        stopService(new Intent(this, LocationService.class));

        isEncours = false; // Met à jour l'état du suivi
        Toast.makeText(this, "Suivi de position arrêté", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        if (isEncours) {
            stopTracking();
        }
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}