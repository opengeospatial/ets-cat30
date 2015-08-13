package org.opengis.cite.cat30;

import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import org.opengis.cite.cat30.util.NamespaceBindings;
import org.opengis.cite.cat30.util.Records;
import org.opengis.cite.cat30.util.SpatialUtils;
import org.opengis.cite.cat30.util.URIUtils;
import org.opengis.cite.cat30.util.XMLUtils;
import org.opengis.cite.geomatics.Extents;
import org.opengis.cite.geomatics.SpatialAssert;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

    private static final Logger LOGR = Logger.getLogger(
            ETSAssert.class.getPackage().getName());

    private ETSAssert() {
    }

    /**
     * Asserts that the qualified name of a DOM Node matches the expected value.
     *
     * @param node The Node to check.
     * @param qName A QName object containing a namespace name (URI) and a local
     * part.
     */
    public static void assertQualifiedName(Node node, QName qName) {
        Assert.assertEquals(node.getLocalName(), qName.getLocalPart(),
                ErrorMessage.get(ErrorMessageKeys.LOCAL_NAME));
        String nsName = (null != node.getNamespaceURI())
                ? node.getNamespaceURI()
                : XMLConstants.NULL_NS_URI;
        Assert.assertEquals(nsName, qName.getNamespaceURI(),
                ErrorMessage.get(ErrorMessageKeys.NAMESPACE_NAME));
    }

    /**
     * Asserts that an XPath 1.0 expression holds true for the given evaluation
     * context. The following standard namespace bindings do not need to be
     * explicitly declared:
     *
     * <ul>
     * <li>ows: {@value org.opengis.cite.cat30.Namespaces#OWS}</li>
     * <li>ows11: {@value org.opengis.cite.cat30.Namespaces#OWS11}</li>
     * <li>xlink: {@value org.opengis.cite.cat30.Namespaces#XLINK}</li>
     * <li>gml: {@value org.opengis.cite.cat30.Namespaces#GML}</li>
     * <li>csw: {@value org.opengis.cite.cat30.Namespaces#CSW}</li>
     * <li>dc: {@value org.opengis.cite.cat30.Namespaces#DCMES}</li>
     * </ul>
     *
     * @param expr A valid XPath 1.0 expression.
     * @param context The context node (Document or Element).
     * @param namespaceBindings A collection of namespace bindings for the XPath
     * expression, where each entry maps a namespace URI (key) to a prefix
     * (value). It may be {@code null}.
     */
    public static void assertXPath(String expr, Node context,
            Map<String, String> namespaceBindings) {
        if (null == context) {
            throw new NullPointerException("Context node is null.");
        }
        NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
        bindings.addAllBindings(namespaceBindings);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(bindings);
        Boolean result;
        try {
            result = (Boolean) xpath.evaluate(expr, context,
                    XPathConstants.BOOLEAN);
        } catch (XPathExpressionException xpe) {
            String msg = ErrorMessage
                    .format(ErrorMessageKeys.XPATH_ERROR, expr);
            LOGR.log(Level.WARNING, msg, xpe);
            throw new AssertionError(msg);
        }
        Element elemNode;
        if (Document.class.isInstance(context)) {
            elemNode = Document.class.cast(context).getDocumentElement();
        } else {
            elemNode = (Element) context;
        }
        Assert.assertTrue(
                result,
                ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT,
                        elemNode.getNodeName(), expr));
    }

    /**
     * Asserts that an XML resource is schema-valid.
     *
     * @param validator The Validator to use.
     * @param source The XML Source to be validated.
     */
    public static void assertSchemaValid(Validator validator, Source source) {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        validator.setErrorHandler(errHandler);
        try {
            validator.validate(source);
        } catch (SAXException | IOException e) {
            throw new AssertionError(ErrorMessage.format(
                    ErrorMessageKeys.XML_ERROR, e.getMessage()));
        }
        Assert.assertFalse(errHandler.errorsDetected(), ErrorMessage.format(
                ErrorMessageKeys.NOT_SCHEMA_VALID, errHandler.getErrorCount(),
                errHandler.toString()));
    }

    /**
     * Asserts that an XML resource satisfies all applicable constraints
     * specified in a Schematron (ISO 19757-3) schema. The "xslt2" query
     * language binding is supported. All patterns are checked.
     *
     * @param schemaRef A URL that denotes the location of a Schematron schema.
     * @param xmlSource The XML Source to be validated.
     */
    public static void assertSchematronValid(URL schemaRef, Source xmlSource) {
        SchematronValidator validator;
        try {
            validator = new SchematronValidator(new StreamSource(
                    schemaRef.toString()), "#ALL");
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder(
                    "Failed to process Schematron schema at ");
            msg.append(schemaRef).append('\n');
            msg.append(e.getMessage());
            throw new AssertionError(msg);
        }
        DOMResult result = validator.validate(xmlSource);
        Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage
                .format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                        validator.getRuleViolationCount(),
                        XMLUtils.writeNodeToString(result.getNode())));
    }

    /**
     * Asserts that the given XML entity contains the expected number of
     * descendant elements having the specified name.
     *
     * @param xmlEntity A Document representing an XML entity.
     * @param elementName The qualified name of the element.
     * @param expectedCount The expected number of occurrences.
     */
    public static void assertDescendantElementCount(Document xmlEntity,
            QName elementName, int expectedCount) {
        NodeList features = xmlEntity.getElementsByTagNameNS(
                elementName.getNamespaceURI(), elementName.getLocalPart());
        Assert.assertEquals(features.getLength(), expectedCount, String.format(
                "Unexpected number of %s descendant elements.", elementName));
    }

    /**
     * Asserts that the given response message contains an OGC exception report.
     * The message body must contain an XML document that has a document element
     * with the following properties:
     *
     * <ul>
     * <li>[local name] = "ExceptionReport"</li>
     * <li>[namespace name] = "http://www.opengis.net/ows/2.0"</li>
     * </ul>
     *
     * @param rsp A ClientResponse object representing an HTTP response message.
     * @param exceptionCode The expected OGC exception code.
     * @param locator A case-insensitive string value expected to occur in the
     * locator attribute (e.g. a parameter name); the attribute value will be
     * ignored if the argument is null or empty.
     */
    public static void assertExceptionReport(ClientResponse rsp,
            String exceptionCode, String locator) {
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document doc = null;
        try {
            doc = rsp.getEntity(Document.class);
        } catch (RuntimeException ex) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to parse response entity. ");
            msg.append(ex.getMessage()).append('\n');
            byte[] body = rsp.getEntity(byte[].class);
            msg.append(new String(body, StandardCharsets.US_ASCII));
            throw new AssertionError(msg);
        }
        String expr = String.format("//ows:Exception[@exceptionCode = '%s']",
                exceptionCode);
        NodeList nodeList = null;
        try {
            nodeList = XMLUtils.evaluateXPath(doc, expr, null);
        } catch (XPathExpressionException xpe) {// won't happen
        }
        Assert.assertTrue(nodeList.getLength() > 0,
                "Expected exception not found in response: " + expr);
        if (null != locator && !locator.isEmpty()) {
            Element exception = (Element) nodeList.item(0);
            String locatorValue = exception.getAttribute("locator").toLowerCase();
            Assert.assertTrue(locatorValue.contains(locator.toLowerCase()),
                    String.format("Expected locator attribute to contain '%s']",
                            locator));
        }
    }

    /**
     * Asserts that all bounding boxes appearing in the search results intersect
     * the given envelope. The following bounding box representations are
     * recognized:
     * <ul>
     * <li>ows:BoundingBox</li>
     * <li>ows:WGS84BoundingBox</li>
     * <li>georss:box (EPSG 4326)</li>
     * <li>georss:where/{http://www.opengis.net/gml}Envelope</li>
     * </ul>
     *
     * @param bbox An envelope specifying a spatial extent in some CRS.
     * @param results A Source object for reading the query results (the
     * document element is typically csw:GetRecordsResponse or atom:feed).
     */
    public static void assertEnvelopeIntersectsBoundingBoxes(final Envelope bbox,
            final Source results) {
        NodeList boxNodeList = null;
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put(Namespaces.GEORSS, "georss");
        nsBindings.put(Namespaces.GML31, "gml31");
        try {
            boxNodeList = (NodeList) XMLUtils.evaluateXPath(results,
                    "//ows:BoundingBox | //ows:WGS84BoundingBox | //georss:box | //gml31:Envelope",
                    nsBindings, XPathConstants.NODESET);
        } catch (XPathExpressionException xpe) { // ignore--expression is ok
        }
        Assert.assertTrue(boxNodeList.getLength() > 0,
                "No bounding box representations (ows:BoundingBox, ows:WGS84BoundingBox, georss:box, gml31:Envelope) found in results.");
        for (int i = 0; i < boxNodeList.getLength(); i++) {
            Node bboxNode = boxNodeList.item(i);
            if (bboxNode.getNamespaceURI().equals(Namespaces.GML31)) {
                bboxNode = SpatialUtils.createGML32Envelope(bboxNode);
            }
            try {
                Envelope envelope;
                if (bboxNode.getNamespaceURI().equals(Namespaces.GEORSS)) {
                    envelope = SpatialUtils.envelopeFromSimpleGeoRSSBox(bboxNode);
                } else {
                    envelope = Extents.createEnvelope(bboxNode);
                }
                SpatialAssert.assertIntersects(bbox, envelope);
            } catch (FactoryException fex) {
                StringBuilder msg = new StringBuilder(
                        "Failed to create envelope from bounding box in result set:\n");
                msg.append(XMLUtils.writeNodeToString(bboxNode));
                throw new AssertionError(msg.toString(), fex);
            }
        }
    }

    /**
     * Asserts that the given response entity includes no search results.
     *
     * @param entity A Document representing a search response (atom:feed,
     * csw:GetRecordsResponse, or rss).
     */
    public static void assertEmptyResultSet(Document entity) {
        String xpath;
        Element docElem = entity.getDocumentElement();
        switch (docElem.getLocalName()) {
            case "feed":
                xpath = "os:totalResults = 0 and count(atom:entry) = 0";
                break;
            case "GetRecordsResponse":
                xpath = "csw:SearchResults/@numberOfRecordsMatched = 0 and count(csw:SearchResults/*) = 0";
                break;
            case "rss":
                xpath = "channel/os:totalResults = 0 and count(channel/item) = 0";
                break;
            default:
                throw new AssertionError("Unknown content: " + docElem.getNodeName());
        }
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put(Namespaces.ATOM, "atom");
        nsBindings.put(Namespaces.OSD11, "os");
        assertXPath(xpath, docElem, nsBindings);
    }

    /**
     * Asserts that the given search terms all occur in the content of each
     * record in the given collection. The context includes the text content of
     * all child elements and their attributes. The comparison is not
     * case-sensitive.
     *
     * @param recordList A list of nodes representing catalog records
     * (csw:Record or atom:entry).
     * @param searchTerms A list of search terms.
     */
    public static void assertAllTermsOccur(NodeList recordList, String... searchTerms) {
        for (String term : searchTerms) {
            for (int i = 0; i < recordList.getLength(); i++) {
                Element record = (Element) recordList.item(i);
                String expr = String.format(
                        "child::*[(text() | attribute::*)[matches(., '%s', 'i')]]",
                        term);
                try {
                    XdmValue result = XMLUtils.evaluateXPath2(
                            new DOMSource(record), expr, null);
                    Assert.assertTrue(result.size() > 0,
                            ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT,
                                    Records.getRecordId(record),
                                    // search term may contain non-ASCII char
                                    expr.replace(term, URIUtils.getPercentEncodedString(term))));
                    LOGR.log(Level.FINE, "In {0} found {1} matching fields for ''{2}'':\n{3}",
                            new Object[]{Records.getRecordId(record), result.size(),
                                term, XMLUtils.writeXdmValueToString(result)});
                } catch (SaxonApiException sae) {
                    throw new AssertionError(String.format(
                            "Failed to evaluate XPath expression: %s \nReason: %s",
                            expr,
                            sae.getMessage()));
                }
            }
        }
    }
}
