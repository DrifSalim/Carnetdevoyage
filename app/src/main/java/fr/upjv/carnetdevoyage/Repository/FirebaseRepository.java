package fr.upjv.carnetdevoyage.Repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import fr.upjv.carnetdevoyage.Model.Point;
import fr.upjv.carnetdevoyage.Model.Voyage;

public class FirebaseRepository {
    FirebaseFirestore db;
    FirebaseAuth auth;
    public FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    public Task<DocumentReference> ajouterVoyage(Voyage voyage) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Gérer le cas où l'utilisateur n'est pas connecté
            return Tasks.forException(new Exception("Utilisateur non connecté"));
        }

        String userId = user.getUid();
        voyage.setUserId(userId);

        // Vérifier s'il y a un voyage en cours
        return db.collection("voyages")
                .whereEqualTo("userId", userId)
                .whereEqualTo("encours", true)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Il y a déjà un voyage en cours
                            return Tasks.forException(new Exception("Un voyage est déjà en cours"));
                        } else {
                            // Aucun voyage en cours, on peut ajouter le nouveau
                            return db.collection("voyages").add(voyage);
                        }
                    } else {
                        // Erreur lors de la requête
                        return Tasks.forException(task.getException());
                    }
                });
    }

    public Task<DocumentSnapshot> recupererVoyage(String voyageId) {
        return db.collection("voyages").document(voyageId).get();
    }

    public Task<QuerySnapshot> recupererPointsDuVoyage(String voyageId) {
        return db.collection("voyages")
                .document(voyageId)
                .collection("points")
                .orderBy("instant")
                .get();
    }

    public Query recupererTousVoyages() { //les voyages d'un utilisateur
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null)
            return db.collection("voyages").whereEqualTo("userId",user.getUid())
                .orderBy("debut", Query.Direction.DESCENDING);
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
        return db.collection("voyages").document(voyageId).collection("points")
                .add(point)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Point", "Point ajouté au voyage " + voyageId + " avec ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Point", "Erreur lors de l'ajout du point au voyage " + voyageId, e);
                });
    }

}
