package fr.upjv.carnetdevoyage;

import com.google.firebase.Timestamp;

public class Point {
    private String idPoint;
    private double longitude;
    private double latitude;
    private Timestamp instant;
    private String idVoyage;

    public Point( double longitude, double latitude, Timestamp instant, String idVoyage) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.instant = instant;
        this.idVoyage = idVoyage;
    }

    public String getIdPoint() {
        return idPoint;
    }

    public void setIdPoint(String idPoint) {
        this.idPoint = idPoint;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Timestamp getInstant() {
        return instant;
    }

    public void setInstant(Timestamp instant) {
        this.instant = instant;
    }

    public String getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(String idVoyage) {
        this.idVoyage = idVoyage;
    }

    @Override
    public String toString() {
        return "Point{" +
                "idPoint=" + idPoint +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", instant=" + instant +
                ", idVoyage=" + idVoyage +
                '}';
    }
}
