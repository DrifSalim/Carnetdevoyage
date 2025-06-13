package fr.upjv.carnetdevoyage;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.upjv.carnetdevoyage.Model.Voyage;

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
        if(unVoyage.isEncours()) {
            this.textViewStatut.setText("En cours");
            this.textViewStatut.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));

        }
        else {
            this.textViewStatut.setText("Terminé");
            this.textViewStatut.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));

        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Timestamp debut = unVoyage.getDebut();
        this.textViewDate.setText("Débuté le : "+dateFormat.format(debut.toDate()));
        if(!unVoyage.isEncours()){
            Timestamp fin = unVoyage.getFin();
                    this.textViewDate.setText("Débuté le : "+dateFormat.format(debut.toDate())+"\n" +
                    "Terminé le : "+dateFormat.format(fin.toDate()));

        }

    }
}
