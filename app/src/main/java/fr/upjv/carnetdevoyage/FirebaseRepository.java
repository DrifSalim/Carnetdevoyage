package fr.upjv.carnetdevoyage;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseRepository {
    FirebaseFirestore db;
    FirebaseAuth auth;
    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    public Task<DocumentReference> ajouterVoyage(Voyage voyage) {
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null){
           voyage.setUserId(user.getUid());
        }
        return db.collection("voyages").add(voyage);
    }

    public Task<DocumentSnapshot> recupererVoyage(String voyageId) {
        return db.collection("voyages").document(voyageId).get();
    }
    public Task<QuerySnapshot> recupererPointsDuVoyage(String voyageId) {
        return db.collection("voyages")
                .document(voyageId)
                .collection("points")
                .get();
    }

    public Query recupererTousVoyages() {
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null)
            return db.collection("voyages").whereEqualTo("userId",user.getUid())
                .orderBy("nom", Query.Direction.ASCENDING);
        return null;
    }
    public Task<Void> updateVoyage(Voyage voyage) {
        if (voyage.getIdVoyage() == null) {
            Log.e("Repository", "Impossible de mettre à jour un voyage sans ID");
            return null;
        }

        return db.collection("voyages")
                .document(voyage.getIdVoyage())
                .set(voyage)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Repository", "Voyage mis à jour: " + voyage.getIdVoyage());
                })
                .addOnFailureListener(e -> {
                    Log.e("Repository", "Erreur lors de la mise à jour du voyage", e);
                });
    }

    //points
    public Task<DocumentReference> addPoint(String voyageId, Point point) {
        // Nous ajoutons directement à la sous-collection du voyage spécifié
        return db.collection("voyages").document(voyageId).collection("points")
                .add(point)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Point", "Point ajouté au voyage " + voyageId + " avec ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Point", "Erreur lors de l'ajout du point au voyage " + voyageId, e);
                });
    }



    // Interface pour la synchronisation
    public interface OnVoyageLoadedListener {
        void onVoyageLoaded(Voyage voyage);
    }
}
