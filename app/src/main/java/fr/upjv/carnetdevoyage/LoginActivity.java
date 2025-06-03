package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        emailEditText = findViewById(R.id.emailValueEditText);
        passwordEditText = findViewById(R.id.motdepasseEditText);
        auth = FirebaseAuth.getInstance();
    }

    public void onClickSeConnecter(View view) {
        String email = emailEditText.getText().toString().trim();
        String motdepasse = passwordEditText.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            emailEditText.setError("Veuillez saisir votre email");
            emailEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(motdepasse)){
            passwordEditText.setError("Veuillez saisir le mot de passe");
            emailEditText.requestFocus();
            return;
        }
        // Connexion avec Firebase
        auth.signInWithEmailAndPassword(email, motdepasse)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        Log.d("Connexion", "signInWithEmail:success");
                        Toast.makeText(this, "Connexion réussie",
                                Toast.LENGTH_SHORT).show();

                        // Redirection vers MainActivity
                        Intent i = new Intent(this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Log.w("Connexion", "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, "Échec de la connexion : " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    public void onClickSinscrire(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}