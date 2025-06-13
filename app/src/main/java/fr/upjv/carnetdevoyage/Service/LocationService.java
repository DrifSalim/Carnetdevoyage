package fr.upjv.carnetdevoyage.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.Timestamp;

import fr.upjv.carnetdevoyage.MainActivity;
import fr.upjv.carnetdevoyage.Model.Point;
import fr.upjv.carnetdevoyage.Repository.FirebaseRepository;

public class LocationService extends Service {
    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private FusedLocationProviderClient fusedLocationClient; //pour récuperer la derniere position connu
    private LocationCallback locationCallback; //pour definir quoi faire à chaque mise à jour de pos
    private FirebaseRepository repository;
    private String voyageId;
    private long frequence = 10000; // valeur par défaut 10secondes

    @Override
    public void onCreate(){
        super.onCreate();
        repository = new FirebaseRepository();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //on recuperè les points des positions
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    Log.d("Service de localisation", "Position: " + location.getLatitude() + ", " + location.getLongitude());

                    // Créer et ajouter le point à la sous-collection
                    Point point = new Point(
                            location.getLongitude(),
                            location.getLatitude(),
                            Timestamp.now(),
                            voyageId
                    );

                    repository.addPoint(voyageId,point);
                }
            }
        };
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            voyageId = intent.getStringExtra("voyageId");
            if (intent.hasExtra("frequence")) {
                frequence = intent.getIntExtra("frequence", 10000);
            }
        }

        if (voyageId == null) {
            Log.e("Service de Localisation", "Aucun voyage spécifié pour le suivi de position");
            stopSelf();
            return START_NOT_STICKY;
        }

        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        requestLocationUpdates();

        return START_STICKY;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Carnet de Voyage")
                .setContentText("Suivi de position en cours...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service de localisation",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void requestLocationUpdates() {
        try {
            //on recupere la position en fonction de la frequence choisie
            LocationRequest locationRequest = new LocationRequest.Builder(frequence*1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(frequence*1000)
                    .build();

            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            Log.e("Service de localisation", "Erreur de permission: " + e.getMessage());
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
