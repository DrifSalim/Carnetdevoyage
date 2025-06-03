package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    private EditText nomEditText, emailEditText, motdepasseEditText,confirmpassEditText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        nomEditText = findViewById(R.id.nomEditText);
        emailEditText = findViewById(R.id.emailEditText);
        motdepasseEditText = findViewById(R.id.MotdePasseEditText);
        confirmpassEditText = findViewById(R.id.confirmMotdePasseEditText);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void onClickSinscrire(View view) {
        String nom = nomEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String motdepasse = motdepasseEditText.getText().toString().trim();
        String confirmpass = confirmpassEditText.getText().toString().trim();
        if(TextUtils.isEmpty(nom)){
            nomEditText.setError("Veuillez saisir votre nom");
            emailEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(email)){
            emailEditText.setError("Veuillez saisir l'email");
            emailEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(motdepasse)){
            motdepasseEditText.setError("Veuillez sasir un mot de passe");
            motdepasseEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(confirmpass)){
            confirmpassEditText.setError("Veuillez confirmer votre mot de passe");
            motdepasseEditText.requestFocus();
            return;
        }
        if (!motdepasse.equals(confirmpass)){
            confirmpassEditText.setError("Les mots de passes ne concordent pas");
            confirmpassEditText.requestFocus();
            return;
        }
        auth.createUserWithEmailAndPassword(email, motdepasse)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Log.d("Inscription","Inscription réussie");
                        FirebaseUser user = auth.getCurrentUser();
                        UserProfileChangeRequest profilUser = new UserProfileChangeRequest.Builder()
                                .setDisplayName(nom)
                                .build();

                        user.updateProfile(profilUser).addOnCompleteListener(profilTask->{
                            // Création d'un document utilisateur dans Firestore
                            createUserDocument(user.getUid(), nom, email);
                        });
                        Toast.makeText(this, "User créée "+user.getDisplayName(), Toast.LENGTH_SHORT).show();

                    }
                    else{
                        Log.e("Inscription","Erreur lors de l'inscription");
                        Toast.makeText(this, "Erreur d'inscription", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createUserDocument(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("displayName", name);
        user.put("email", email);
        user.put("createdAt", com.google.firebase.Timestamp.now());
        user.put("voyageIds", new ArrayList<>());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Inscription réussie",
                            Toast.LENGTH_SHORT).show();

                    // Redirection vers MainActivity
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Document User", "Error creating user document", e);
                    Toast.makeText(RegisterActivity.this, "Erreur : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
    public void onClickSeConnecter(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}