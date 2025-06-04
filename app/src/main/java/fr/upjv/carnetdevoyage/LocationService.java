package fr.upjv.carnetdevoyage;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;

public class LocationService extends Service {
    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
