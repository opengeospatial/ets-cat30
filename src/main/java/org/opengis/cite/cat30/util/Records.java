package org.opengis.cite.cat30.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.opengis.cite.cat30.Namespaces;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class contains various utility methods for querying or reading
 * representations of catalog records.
 */
public class Records {

    /**
     * Generates a random sequence of 5-14 characters in the range [a-z]. Such
     * text is very unlikely to match the content of any record fields.
     *
     * @return A String containing lower case letters (Latin alphabet).
     */
    public static String generateRandomText() {
        Random r = new Random();
        int length = r.nextInt(10) + 5;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char letter = (char) (r.nextInt(26) + 'a');
            str.append(letter);
        }
        return str.toString();
    }

    /**
     * Gets the identifier of the given record representation.
     *
     * @param record An Element node representing an entry in a result set
     * (csw:Record or atom:entry).
     * @return The element name with a record identifier appended (the first
     * identifier if there is more than one).
     */
    public static String getRecordId(Element record) {
        StringBuilder str = new StringBuilder(record.getNodeName());
        NodeList idList = record.getElementsByTagNameNS(Namespaces.DCMES, "identifier");
        if (idList.getLength() == 0) {
            idList = record.getElementsByTagNameNS(Namespaces.ATOM, "id");
        }
        if (idList.getLength() > 0) {
            str.append("[").append(idList.item(0).getTextContent()).append("]");
        }
        return str.toString();
    }

    /**
     * Evaluates an XPath (2.0) expression against the sample data and returns
     * the results as a list of string values.
     *
     * @param file A File containing catalog data (csw:GetRecordsResponse).
     * @param xpath An XPath expression that is expected to denote a simple
     * record property; the namespace prefixes "dc" and "dct" may be used (e.g.
     * //dc:title).
     * @return A list of property values, which may be empty if none are found
     * in the sample data.
     */
    public static List<String> findPropertyValues(File file, String xpath) {
        List<String> idList = new ArrayList<>();
        Source src = new StreamSource(file);
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put(Namespaces.DCMES, "dc");
        nsBindings.put(Namespaces.DCMI, "dct");
        XdmValue value = null;
        try {
            value = XMLUtils.evaluateXPath2(src, xpath, nsBindings);
        } catch (SaxonApiException ex) {
            Logger.getLogger(DatasetInfo.class.getName()).log(Level.WARNING,
                    "Failed to evaluate XPath expression: " + xpath, ex);
        }
        for (XdmItem item : value) {
            idList.add(item.getStringValue());
        }
        return idList;
    }

    /**
     * Finds matching search terms corresponding to the given record elements.
     * One term is selected for each element. If there are no records that
     * contain values for all elements, two randomly selected title words are
     * returned instead.
     *
     * @param dataFile A file containing sample data (csw:Record
     * representations).
     * @param elemNames A list of QName objects denoting the qualified names of
     * record elements.
     * @return A string containing two or more terms (separated by a space
     * character).
     */
    public static String findMatchingSearchTerms(File dataFile, QName... elemNames) {
        StringBuilder searchTerms = new StringBuilder();
        XdmValue results = findRecordsInSampleData(dataFile, elemNames);
        if (results.size() > 0) {
            XdmNode lastNode = (XdmNode) results.itemAt(results.size() - 1);
            for (QName elemName : elemNames) {
                String value = lastNode.axisIterator(Axis.CHILD,
                        new net.sf.saxon.s9api.QName(elemName)).next().getStringValue();
                // element content may contain multiple words
                searchTerms.append(value.trim().split("\\s+")[0]).append(" ");
            }
        } else { // fallback: use randomly selected title words
            String[] titleWords;
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<String> titles = findPropertyValues(dataFile, "//dc:title");
            do {
                int randomIndex = random.nextInt(titles.size());
                titleWords = titles.get(randomIndex).trim().split("\\s+");
            } while (titleWords.length < 2);
            searchTerms.append(titleWords[titleWords.length - 1]).append(" ");
            searchTerms.append(titleWords[0]);
        }
        return searchTerms.toString().trim();
    }

    /**
     * Finds records in the sample data that contain all of the specified child
     * elements (some of which may occur more than once).
     *
     * @param dataFile A file containing sample data (csw:Record
     * representations).
     * @param properties A list of QName objects denoting the qualified names of
     * record elements.
     * @return An immutable XdmValue object containing zero or more matching
     * nodes (XdmItem).
     */
    public static XdmValue findRecordsInSampleData(File dataFile,
            QName... properties) {
        StringBuilder xpath = new StringBuilder("//csw:Record[");
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put(Namespaces.CSW, "csw");
        NamespaceBindings stdBindings = NamespaceBindings.withStandardBindings();
        for (int i = 0; i < properties.length; i++) {
            QName elemName = properties[i];
            String localName = elemName.getLocalPart();
            String nsName = elemName.getNamespaceURI();
            String nsPrefix = stdBindings.getPrefix(nsName);
            nsBindings.put(nsName, nsPrefix);
            xpath.append(nsPrefix).append(':').append(localName);
            if (i < (properties.length - 1)) {
                xpath.append(" and ");
            }
        }
        xpath.append("]");
        XdmValue value = null;
        try {
            value = XMLUtils.evaluateXPath2(new StreamSource(dataFile),
                    xpath.toString(), nsBindings);
        } catch (SaxonApiException x) {
            Logger.getLogger(Records.class.getName()).log(Level.WARNING,
                    String.format(
                            "Failed to evaluate XPath expression against sample data file at %s:\n %s ",
                            dataFile.getAbsolutePath(), xpath), x);
        }
        return value;
    }

    /**
     * Returns the name of the catalog record corresponding to the specified
     * media type.
     *
     * @param mediaType A string identifying a media type.
     * @return The qualified name of the element representing a catalog record.
     *
     * @see
     * <a href="http://www.iana.org/assignments/media-types/" target="_blank">IANA
     * Media Types Register</a>
     */
    public static QName getRecordName(String mediaType) {
        QName recordName;
        if (mediaType.startsWith(MediaType.APPLICATION_ATOM_XML)) {
            recordName = new QName(Namespaces.ATOM, "entry");
        } else if (mediaType.startsWith("application/rss+xml")) {
            recordName = new QName(XMLConstants.NULL_NS_URI, "item");
        } else {
            recordName = new QName(Namespaces.CSW, "Record");
        }
        return recordName;
    }
}
