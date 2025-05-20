package fr.upjv.carnetdevoyage;

import com.google.firebase.Timestamp;

public class Point {
    private int idPoint;
    private double longitude;
    private double latitude;
    private Timestamp instant;
    private int idVoyage;

    public Point(int idPoint, double longitude, double latitude, Timestamp instant, int idVoyage) {
        this.idPoint = idPoint;
        this.longitude = longitude;
        this.latitude = latitude;
        this.instant = instant;
        this.idVoyage = idVoyage;
    }

    public int getIdPoint() {
        return idPoint;
    }

    public void setIdPoint(int idPoint) {
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

    public int getIdVoyage() {
        return idVoyage;
    }

    public void setIdVoyage(int idVoyage) {
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
