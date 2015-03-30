package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.Envelope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the SpatialUtils class.
 */
public class VerifySpatialUtils {

    private static DocumentBuilder docBuilder;

    public VerifySpatialUtils() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void createEnvelopeFromGeoRSSBox() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/georss/box.xml"));
        Envelope env = SpatialUtils.envelopeFomGeoRSSBox(doc.getDocumentElement());
        assertEquals("Unexpected CRS code", "WGS 84", env.getCoordinateReferenceSystem().getName().getCode());
        assertArrayEquals("Unexpected coords for upper corner.",
                new double[]{33.5, -116.2},
                env.getUpperCorner().getCoordinate(),
                0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEnvelopeFromBoxWithMissingCorner() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/georss/box-missingCorner.xml"));
        Envelope env = SpatialUtils.envelopeFomGeoRSSBox(doc.getDocumentElement());
        System.out.println(env.toString());
        assertNull(env);
    }

    @Test(expected = NumberFormatException.class)
    public void createEnvelopeFromBoxWithNonNumericValue() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/georss/box-NaN.xml"));
        Envelope env = SpatialUtils.envelopeFomGeoRSSBox(doc.getDocumentElement());
        assertNull(env);
    }
}
