package fr.upjv.carnetdevoyage.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

public class Voyage {
    @DocumentId
    private String idVoyage;
    private String nom;
    private String description;
    private int frequence;
    private Timestamp debut;
    private Timestamp fin;
    private boolean encours;
    private List<Point> position;
    private String userId;
    public Voyage () {}
    public Voyage(String nom, String description, int frequence) {
        this.nom = nom;
        this.description = description;
        this.frequence = frequence;
        this.debut = Timestamp.now();
        this.encours=true;
        this.position = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



    public boolean isEncours() {
        return encours;
    }

    public void setEncours(boolean encours) {
        this.encours = encours;
    }

    public String getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(String idVoyage) {
        this.idVoyage = idVoyage;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFrequence() {
        return frequence;
    }

    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }

    public Timestamp getDebut() {
        return debut;
    }

    public void setDebut(Timestamp debut) {
        this.debut = debut;
    }

    public Timestamp getFin() {
        return fin;
    }

    public void setFin(Timestamp fin) {
        this.fin = fin;
    }

    public List<Point> getPosition() {
        return position;
    }

    public void setPosition(List<Point> position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Voyage{" +
                "idVoyage=" + idVoyage +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", frequence=" + frequence +
                ", debut=" + debut +
                ", fin=" + fin +
                ", encours=" + encours +
                ", position=" + position +
                '}';
    }

    public void terminerVoyage(){
        this.encours=false;
        this.fin=Timestamp.now();
    }
}
