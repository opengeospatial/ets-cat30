package org.opengis.cite.cat30.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.opengis.cite.geomatics.Extents;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides information about the data held by the IUT.
 */
public class DatasetInfo {

    private final File dataFile;
    private Envelope geographicExtent;
    private List<String> recordIdentifiers;
    private List<String> recordTitles;
    private List<String> topics;

    public DatasetInfo(File dataFile) {
        if (!dataFile.isFile()) {
            throw new IllegalArgumentException("Data file does not exist at "
                    + dataFile.getAbsolutePath());
        }
        QName docElemName = XMLUtils.nameOfDocumentElement(new StreamSource(dataFile));
        if (!docElemName.getLocalPart().equals("GetRecordsResponse")) {
            Logger.getLogger(DatasetInfo.class.getName()).log(Level.WARNING,
                    "File does not contain a GetRecords response: {0}", docElemName);
        }
        this.dataFile = dataFile;
    }

    /**
     * Returns the file containing the sample data.
     *
     * @return A File object representing a normal file.
     */
    public File getDataFile() {
        return dataFile;
    }

    /**
     * Returns an Envelope representing the total geographic extent of the
     * sample data. The bounding boxes (ows:BoundingBox or ows:WGS84BoundingBox)
     * for each record are coalesced to determine the overall extent.
     *
     * @return An Envelope in some supported CRS.
     */
    public Envelope getGeographicExtent() {
        if (null == this.geographicExtent) {
            this.geographicExtent = calculateTotalExtent(dataFile);
        }
        return geographicExtent;
    }

    /**
     * Returns a sequence of record identifiers (dc:identifier) found in the
     * sample data.
     *
     * @return A List containing all element values.
     */
    public List<String> getRecordIdentifiers() {
        if (null == this.recordIdentifiers) {
            this.recordIdentifiers = Records.findPropertyValues(dataFile,
                    "//dc:identifier");
        }
        return recordIdentifiers;
    }

    /**
     * Returns a sequence of record titles (dc:title) found in the sample data.
     * At least one such element must appear in every record representation.
     *
     * @return A List containing all element values.
     */
    public List<String> getRecordTitles() {
        if (null == this.recordTitles) {
            this.recordTitles = Records.findPropertyValues(dataFile, "//dc:title");
        }
        return recordTitles;
    }

    /**
     * Returns a sequence of topic (dc:subject) values found in the sample data.
     * A record may contain zero or more subject elements that convey a set of
     * topics (e.g. keywords, key phrases, classification codes) that apply to
     * it.
     *
     * @return A List containing all topic values.
     */
    public List<String> getRecordTopics() {
        if (null == this.topics) {
            this.topics = Records.findPropertyValues(dataFile, "//dc:subject");
        }
        return topics;
    }

    /**
     * Calculates the total extent of the records in the sample data. Each
     * csw:Record element may contain at least one ows:BoundingBox (or
     * ows:WGS84BoundingBox) element that describes the spatial coverage of a
     * catalogued resource.
     *
     * @param file A File containing catalog data (csw:GetRecordsResponse).
     * @return An Envelope representing the total geographic extent of the
     * sample data, or null if no bounding boxes exist in the data.
     */
    Envelope calculateTotalExtent(File file) {
        Source src = new StreamSource(dataFile);
        NodeList nodeList = null;
        try {
            nodeList = (NodeList) XMLUtils.evaluateXPath(src,
                    "//csw:Record/ows:BoundingBox[1] | //csw:Record/ows:WGS84BoundingBox[1]",
                    null, XPathConstants.NODESET);
        } catch (XPathExpressionException xpe) {
            TestSuiteLogger.log(Level.WARNING, "getBoundingBoxes: ", xpe);
        }
        if (nodeList.getLength() == 0) {
            return null;
        }
        List<Node> boxNodes = XMLUtils.getNodeListAsList(nodeList);
        Envelope extent;
        try {
            extent = Extents.coalesceBoundingBoxes(boxNodes);
        } catch (FactoryException | TransformException ex) {
            StringBuilder msg = new StringBuilder("Failed to coalesce bounding boxes. ");
            msg.append(new String(ex.getMessage().getBytes(),
                    StandardCharsets.US_ASCII));
            throw new RuntimeException(msg.toString());
        }
        return extent;
    }

}
