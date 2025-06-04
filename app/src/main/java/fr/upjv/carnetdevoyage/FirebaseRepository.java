package fr.upjv.carnetdevoyage;

import android.widget.Toast;

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
            return db.collection("voyages")
                .orderBy("nom", Query.Direction.ASCENDING);
    }



}
