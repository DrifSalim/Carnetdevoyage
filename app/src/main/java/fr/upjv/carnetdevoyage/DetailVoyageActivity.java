package fr.upjv.carnetdevoyage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.Manifest;

import fr.upjv.carnetdevoyage.Model.Point;
import fr.upjv.carnetdevoyage.Model.Voyage;
import fr.upjv.carnetdevoyage.Repository.FirebaseRepository;
import fr.upjv.carnetdevoyage.Service.LocationService;
import fr.upjv.carnetdevoyage.utils.VoyageFileGenerator;

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
    private boolean isEncours;
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
        mapView.getMapAsync(this); //recuperer une instance de la map

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
                            currentVoyage = voyage;
                            this.isEncours = voyage.isEncours();
                            //Afficher les informations du voyage dans l'interface
                            afficherInfoVoyage(voyage);

                            //charger les points du voyage
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
            buttonExport.setVisibility(View.GONE);

        } else {
            textViewStatus.setText("Terminé");
            textViewStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            buttonEnregistrerPosition.setVisibility(View.GONE);
            buttonArreterVoyage.setVisibility(View.GONE);
            buttonExport.setVisibility(View.VISIBLE);

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
                    this.currentVoyage.setPosition(points);
                    updateMapWithVoyagePath(points);

                    Log.d("DetailVoyage", "Points chargés: " + points.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailVoyage", "Erreur lors du chargement des points", e);
                    Toast.makeText(this, "Erreur lors du chargement des points", Toast.LENGTH_SHORT).show();
                });
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
                // Si la permission est accordée, réessayer de centrer la carte ou d'enregistrer la position
                if (currentVoyage != null && currentVoyage.getPosition() != null && !currentVoyage.getPosition().isEmpty()) {
                    updateMapWithVoyagePath(currentVoyage.getPosition());
                } else {
                    centerMapOnUserLocationOrDefault();
                }
            } else {
                Toast.makeText(this, "Permission de localisation requise pour certaines fonctionnalités.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (currentVoyage != null && currentVoyage.getPosition() != null && !currentVoyage.getPosition().isEmpty()) {
            //Si un voyage est deja en cours et pas vide on affiche le voyage sur la map
            updateMapWithVoyagePath(currentVoyage.getPosition());
        } else {
            //Si non on affiche juste la localisation de l'utilisateur
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
        //Polyline pour la ligne
        PolylineOptions polylineOptions = new PolylineOptions();
        //Pour centrer la map sur le chemin
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (Point point : points) {
            LatLng position = new LatLng(point.getLatitude(), point.getLongitude());
            polylineOptions.add(position);
            boundsBuilder.include(position);
        }

        polylineOptions.color(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        polylineOptions.width(8);
        //afficher la ligne
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
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
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
        // Vérifie si le voyage est en cours avant d'enregistrer une position manuelle
        if (currentVoyage == null || !currentVoyage.isEncours()) {
            Toast.makeText(this, "Le voyage n'est pas en cours. Impossible d'enregistrer une position.", Toast.LENGTH_SHORT).show();
            return;
        }



        // Vérifier la permission de localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // La permission n'est pas accordée, on va la redemander
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Crée un nouvel objet Point
                        Point newPoint = new Point();
                        newPoint.setLatitude(location.getLatitude());
                        newPoint.setLongitude(location.getLongitude());
                        newPoint.setInstant(new Timestamp(new Date()));

                        // Enregistrer le Point dans Firebase
                        db.addPoint(voyageId, newPoint)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(DetailVoyageActivity.this, "Position enregistrée !", Toast.LENGTH_SHORT).show();
                                    Log.d("Service de localisation", "Position: " + location.getLatitude() + ", " + location.getLongitude());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("DetailVoyage", "Erreur lors de l'enregistrement du point", e);
                                    Toast.makeText(DetailVoyageActivity.this, "Erreur lors de l'enregistrement de la position.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Impossible d'obtenir la position actuelle. Veuillez réessayer.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailVoyage", "Erreur lors de la récupération de la position pour enregistrement", e);
                    Toast.makeText(this, "Erreur de localisation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void onClickExporter(View view) {
        // Vérifier qu'il y a des données à exporter
        if (currentVoyage == null || currentVoyage.getPosition() == null || currentVoyage.getPosition().isEmpty()) {
            Toast.makeText(this, "Aucun point enregistré à exporter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dialog pour choisir le format d'export
        String[] options = {"KML", "GPX"};
        new AlertDialog.Builder(this)
                .setTitle("Choisir le format d'export")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if (position == 0) {
                            exportAndSendByEmail("KML");
                        } else {
                            exportAndSendByEmail("GPX");
                        }
                    }
                })
                .show();
    }

    private void exportAndSendByEmail(String format) {
        try {
            // Générer le contenu du fichier (classe VoyageFileGenerator qui est dans util)
            String fileContent;
            if (format.equals("KML")) {
                fileContent = VoyageFileGenerator.generateKml(currentVoyage, currentVoyage.getPosition());
            } else {
                fileContent = VoyageFileGenerator.generateGpx(currentVoyage, currentVoyage.getPosition());
            }

            //Créer le fichier sur le téléphone
            File file = createFileFromContent(fileContent, format);

            //Envoyer par email
            if (file != null) {
                sendEmailWithAttachment(file, format);
            }

        } catch (Exception e) {
            Log.e("Export", "Erreur lors de l'export: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors de l'export: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createFileFromContent(String content, String format) throws IOException {
        // Créer le dossier d'export
        File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        // Créer le nom du fichier avec la date
        SimpleDateFormat dateNameFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String dateFormatee = dateNameFormat.format(currentVoyage.getDebut().toDate());
        String fileName = "Voyage_"+currentVoyage.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "_" + dateFormatee;

        // Créer le fichier avec la bonne extension
        String extension = format.equals("KML") ? ".kml" : ".gpx";
        File file = new File(exportDir, fileName + extension);

        // Écrire le contenu dans le fichier
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();

        Log.d("Export", "Fichier créé: " + file.getAbsolutePath());
        return file;
    }

    private void sendEmailWithAttachment(File file, String format) {
        try {
            // Créer l'URI du fichier pour le partage
            Uri fileUri = FileProvider.getUriForFile(this,
                    "fr.upjv.carnetdevoyage.fileprovider", file);

            // Créer l'intent pour envoyer un email
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            //emailIntent.setData(Uri.parse("mailto:")); mail to ne gère pas les pièces jointes c pour ca on la commenté
            emailIntent.setType("message/rfc822"); //MIME pour le format emails
            // Préparer le sujet et le message
            String subject = "Voyage "+currentVoyage.getNom()+" - Exporté en " + format;
            String body = createEmailBody();

            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

            // Donner permission de lecture pour le fichier dans le flag
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(emailIntent, "Envoyer le fichier par email"));
        } catch (Exception e) {
            Log.e("Email", "Erreur lors de l'envoi: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors de l'envoi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String createEmailBody() {
        StringBuilder body = new StringBuilder();
        body.append("Voici mon voyage : \"").append(currentVoyage.getNom()).append("\".\n\n");
        body.append("Description : ").append(currentVoyage.getDescription()).append("\n");

        // Date de début
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        body.append("Que j'ai commencé le : ").append(dateFormat.format(currentVoyage.getDebut().toDate())).append("\n");

        // Date de fin si le voyage est terminé
        if (!currentVoyage.isEncours() && currentVoyage.getFin() != null) {
            body.append("et Fini le : ").append(dateFormat.format(currentVoyage.getFin().toDate())).append("\n");
        }

        body.append("Nombre de points enregistrés : ").append(currentVoyage.getPosition().size()).append("\n");

        return body.toString();
    }

}