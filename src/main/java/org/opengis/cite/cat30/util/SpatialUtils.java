package org.opengis.cite.cat30.util;

import java.util.logging.Level;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.referencing.CRS;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.w3c.dom.Node;

/**
 * Utility methods pertaining to geographic data representations.
 */
public class SpatialUtils {

    /**
     * Creates an Envelope from a georss:box element. The coordinate reference
     * system is EPSG 4326 (lat,lon axis order).
     *
     * @param boxNode An Element node (georss:box) containing the coordinates of
     * the lower and upper corners.
     * @return An Envelope object representing the given spatial extent, or null
     * if it cannot be constructed (a runtime exception may have occurred).
     */
    public static Envelope envelopeFomGeoRSSBox(Node boxNode) {
        if (!boxNode.getNamespaceURI().equals(Namespaces.GEORSS)) {
            throw new IllegalArgumentException("Not a GeoRSS element.");
        }
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode("EPSG:4326");
        } catch (FactoryException ex) {
            TestSuiteLogger.log(Level.WARNING, "Failed to create CRS: EPSG 4326", ex);
        }
        GeneralEnvelope env = new GeneralEnvelope(crs);
        String[] coords = boxNode.getTextContent().trim().split("\\s+");
        if (coords.length != 4) {
            throw new IllegalArgumentException(
                    "Expected two coordinate tuples (lower and upper corners).");
        }
        double[] coordArray = new double[coords.length];
        for (int i = 0; i < coords.length; i++) {
            coordArray[i] = Double.parseDouble(coords[i]);
        }
        env.setEnvelope(coordArray);
        return env;
    }

}
