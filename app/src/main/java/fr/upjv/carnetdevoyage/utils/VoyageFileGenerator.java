package fr.upjv.carnetdevoyage.utils; // Vous pouvez créer un package 'utils' ou 'export'

import fr.upjv.carnetdevoyage.Model.Point;
import fr.upjv.carnetdevoyage.Model.Voyage;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone; // Importation ajoutée pour TimeZone

public class VoyageFileGenerator {


     //Génèret le contenu KML pour un voyage donné à partir d'une liste de points d'un voyage.
    public static String generateKml(Voyage voyage, List<Point> points) {
        StringBuilder kml = new StringBuilder();
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        kml.append("<Document>\n");
        kml.append("  <name>").append(voyage.getNom()).append("</name>\n");
        kml.append("  <description>").append(voyage.getDescription()).append("</description>\n");

        // Ajout des points individuels
        SimpleDateFormat kmlSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US); // Format KML
        kmlSdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (Point point : points) {
            kml.append("  <Placemark>\n");
            kml.append("    <name>Point ").append(kmlSdf.format(point.getInstant().toDate())).append("</name>\n");
            kml.append("    <Point>\n");
            kml.append("      <coordinates>")
                    .append(point.getLongitude()).append(",")
                    .append(point.getLatitude()).append(",0</coordinates>\n"); // 0 pour l'altitude par défaut
            kml.append("    </Point>\n");
            kml.append("  </Placemark>\n");
        }

        // Ajout du tracé (LineString)
        kml.append("  <Placemark>\n");
        kml.append("    <name>Tracé du Voyage</name>\n");
        kml.append("    <LineString>\n");
        kml.append("      <altitudeMode>clampToGround</altitudeMode>\n"); // Fixé au sol
        kml.append("      <coordinates>");
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            kml.append(point.getLongitude()).append(",").append(point.getLatitude()).append(",0");
            if (i < points.size() - 1) {
                kml.append(" "); // Espace entre les coordonnées
            }
        }
        kml.append("</coordinates>\n");
        kml.append("    </LineString>\n");
        kml.append("  </Placemark>\n");

        kml.append("</Document>\n");
        kml.append("</kml>");
        return kml.toString();
    }


     // Génère le contenu GPX pour un voyage donné à partir d'une liste de points.
    public static String generateGpx(Voyage voyage, List<Point> points) {
        StringBuilder gpx = new StringBuilder();
        SimpleDateFormat gpxSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US); // Format GPX
        gpxSdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx\n");
        gpx.append(" xmlns=\"http://www.topografix.com/GPX/1/1\"\n");
        gpx.append(" version=\"1.1\"\n");
        gpx.append(" creator=\"CarnetDeVoyageApp\">\n");
        gpx.append("  <metadata>\n");
        gpx.append("    <name>").append(voyage.getNom()).append("</name>\n");
        gpx.append("    <desc>").append(voyage.getDescription()).append("</desc>\n");
        //ajouter un horodatage pour les métadonnées du fichier
        gpx.append("    <time>").append(gpxSdf.format(new Date())).append("</time>\n");
        gpx.append("  </metadata>\n");

        gpx.append("  <trk>\n");
        gpx.append("    <name>").append(voyage.getNom()).append(" Track</name>\n");
        gpx.append("    <trkseg>\n");
        for (Point point : points) {
            gpx.append("      <trkpt lat=\"").append(point.getLatitude()).append("\" lon=\"").append(point.getLongitude()).append("\">\n");
            gpx.append("        <time>").append(gpxSdf.format(point.getInstant().toDate())).append("</time>\n");
            gpx.append("      </trkpt>\n");
        }
        gpx.append("    </trkseg>\n");
        gpx.append("  </trk>\n");

        gpx.append("</gpx>");
        return gpx.toString();
    }
}
