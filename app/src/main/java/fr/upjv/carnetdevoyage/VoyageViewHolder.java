package fr.upjv.carnetdevoyage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class VoyageViewHolder extends RecyclerView.ViewHolder {
    private TextView textViewNom;
    private TextView textViewDescription;

    private TextView textViewDate;
    private TextView textViewStatut;
    public VoyageViewHolder(@NonNull View itemView) {
        super(itemView);


        this.textViewNom=itemView.findViewById(R.id.textViewNom);
        this.textViewDescription=itemView.findViewById(R.id.textViewDescription);
        this.textViewDate=itemView.findViewById(R.id.textViewDate);
        this.textViewStatut=itemView.findViewById(R.id.textViewStatut);

    }
    public void mettreAjourLigne(Voyage unVoyage){
        this.textViewNom.setText(unVoyage.getNom());
        this.textViewDescription.setText(""+unVoyage.getDescription());
        this.textViewStatut.setText(""+unVoyage.isEncours());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Timestamp debut = unVoyage.getDebut();
        this.textViewDate.setText("Débuté le : "+dateFormat.format(debut.toDate()));

    }
}
