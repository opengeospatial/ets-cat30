package org.opengis.cite.cat30.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.referencing.CRS;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility methods pertaining to geographic data representations.
 */
public class SpatialUtils {

    /**
     * Creates an Envelope from a simple georss:box element. The coordinate
     * reference system is EPSG 4326 (lat,lon axis order).
     *
     * @param boxNode An Element node (georss:box) containing the coordinates of
     * the lower and upper corners.
     * @return An Envelope object representing the given spatial extent, or null
     * if it cannot be constructed (a runtime exception may have occurred).
     *
     * @see <a href="http://www.georss.org/simple.html" target="_blank">GeoRSS
     * Simple</a>
     */
    public static Envelope envelopeFromSimpleGeoRSSBox(Node boxNode) {
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

    /**
     * Creates a GML 3.2 envelope from a legacy GML 3.1 representation.
     *
     * @param oldEnvNode An element node representing a GML 3.1 envelope.
     * @return A new gml:Envelope element, or null if the source node is not a
     * GML 3.1 envelope.
     */
    public static Element createGML32Envelope(Node oldEnvNode) {
        if (!oldEnvNode.getNamespaceURI().equals(Namespaces.GML31)) {
            return null;
        }
        Element oldEnvelope = (Element) oldEnvNode;
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SpatialUtils.class.getName()).log(Level.WARNING, null, ex);
        }
        Document doc = builder.newDocument();
        Element envelope = doc.createElementNS(Namespaces.GML, "Envelope");
        envelope.setAttribute("srsName", oldEnvelope.getAttribute("srsName"));
        Element lowerCorner = doc.createElementNS(Namespaces.GML, "lowerCorner");
        lowerCorner.setTextContent(oldEnvelope.getElementsByTagNameNS(
                Namespaces.GML31, "lowerCorner").item(0).getTextContent());
        envelope.appendChild(lowerCorner);
        Element upperCorner = doc.createElementNS(Namespaces.GML, "upperCorner");
        upperCorner.setTextContent(oldEnvelope.getElementsByTagNameNS(
                Namespaces.GML31, "upperCorner").item(0).getTextContent());
        envelope.appendChild(upperCorner);
        return envelope;
    }
}
