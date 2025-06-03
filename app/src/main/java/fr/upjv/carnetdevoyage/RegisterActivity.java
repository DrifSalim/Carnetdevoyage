package fr.upjv.carnetdevoyage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class RegisterActivity extends AppCompatActivity {
    EditText nomEditText, emailEditText, motdepasseEditText,confirmpassEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        nomEditText = findViewById(R.id.nomEditText);
        emailEditText = findViewById(R.id.emailEditText);
        motdepasseEditText = findViewById(R.id.MotdePasseEditText);
        confirmpassEditText = findViewById(R.id.confirmMotdePasseEditText);
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

    }

    public void onClickSeConnecter(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}