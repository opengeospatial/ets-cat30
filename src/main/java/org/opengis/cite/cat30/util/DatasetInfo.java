package org.opengis.cite.cat30.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.opengis.cite.cat30.Namespaces;
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

    public DatasetInfo(File dataFile) {
        QName docElemName = XMLUtils.nameOfDocumentElement(new StreamSource(dataFile));
        if (!docElemName.getLocalPart().equals("GetRecordsResponse")) {
            Logger.getLogger(DatasetInfo.class.getName()).log(Level.WARNING,
                    "File does not contain a GetRecords response: {0}", docElemName);
        }
        this.dataFile = dataFile;
    }

    public File getDataFile() {
        return dataFile;
    }

    public Envelope getGeographicExtent() {
        if (null == this.geographicExtent) {
            this.geographicExtent = calculateTotalExtent(dataFile);
        }
        return geographicExtent;
    }

    public List<String> getRecordIdentifiers() {
        if (null == this.recordIdentifiers) {
            this.recordIdentifiers = findRecordIdentifiers(dataFile);
        }
        return recordIdentifiers;
    }

    /**
     * Extracts the record identifiers that occur in the sample data obtained
     * from the IUT. Each csw:Record element must contain at least one
     * dc:identifier element.
     *
     * @param file A File containing catalog data (csw:GetRecordsResponse).
     * @return A list of record identifiers; the list may be empty if none are
     * found in the sample data.
     */
    List<String> findRecordIdentifiers(File file) {
        List<String> idList = new ArrayList<>();
        Source src = new StreamSource(dataFile);
        Map<String, String> nsBindings = Collections.singletonMap(Namespaces.DCMES, "dc");
        XdmValue value = null;
        try {
            value = XMLUtils.evaluateXPath2(src, "//dc:identifier", nsBindings);
        } catch (SaxonApiException ex) {
            Logger.getLogger(DatasetInfo.class.getName()).log(Level.WARNING,
                    "Failed to evaluate XPath expression.", ex);
        }
        for (XdmItem item : value) {
            idList.add(item.getStringValue());
        }
        return idList;
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
            throw new RuntimeException("Failed to coalesce bounding boxes.", ex);
        }
        return extent;
    }

}
