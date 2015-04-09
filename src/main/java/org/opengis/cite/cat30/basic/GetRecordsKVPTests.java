package org.opengis.cite.cat30.basic;

import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;
import org.geotoolkit.geometry.Envelopes;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.DatasetInfo;
import org.opengis.cite.cat30.util.Records;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.opengis.cite.cat30.util.XMLUtils;
import org.opengis.cite.geomatics.Extents;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides tests that apply to the <code>GetRecords</code> request using the
 * KVP syntax.
 *
 * <p>
 * The KVP syntax must be supported; this encoding is generally used with the
 * GET method but may also be used with the POST method; however a POST method
 * binding is very unusual and will be advertised in the capabilities document
 * as an operational constraint as indicated below. The media type of a KVP
 * request entity is "application/x-www-form-urlencoded".
 * </p>
 *
 * @see "OGC 12-176r6, 7.3: GetRecords operation"
 */
public class GetRecordsKVPTests extends CommonFixture {

    /**
     * Service endpoint for GetRecords using the GET method.
     */
    private URI getURI;
    /**
     * Service endpoint for GetRecords using the POST method.
     */
    private URI postURI;
    /**
     * An Envelope defining the total geographic extent of the sample data.
     */
    private Envelope geoExtent;
    /**
     * A list of record titles retrieved from the IUT.
     */
    private List<String> recordTitles;
    /**
     * A list of record identifiers retrieved from the IUT.
     */
    private List<String> recordIdentifiers;

    void setExtent(Envelope extent) {
        this.geoExtent = extent;
    }

    void setGetEndpoint(URI uri) {
        this.getURI = uri;
    }

    /**
     * Finds the GET and POST method endpoints for the GetRecords request in the
     * capabilities document.
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void findRequestEndpoints(ITestContext testContext) {
        this.getURI = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_RECORDS, HttpMethod.GET);
        if (null == this.getURI.getScheme()) {
            throw new SkipException("GET endpoint for GetRecords request not found.");
        }
        this.postURI = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_RECORDS, HttpMethod.POST);
    }

    /**
     * Gets information about the sample data obtained from the IUT, including:
     * its total geographic extent; a collection of record titles; and a set of
     * record identifiers. Each csw:Record element may contain at least one
     * ows:BoundingBox (or ows:WGS84BoundingBox) element that describes the
     * spatial coverage of a catalogued resource.
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void getDatasetInfo(ITestContext testContext) {
        DatasetInfo dataset = (DatasetInfo) testContext.getSuite().getAttribute(
                SuiteAttribute.DATASET.getName());
        if (null == dataset) {
            throw new SkipException("Dataset info not found in test context.");
        }
        Envelope env = dataset.getGeographicExtent();
        setExtent(env);
        this.recordTitles = dataset.getRecordTitles();
        this.recordIdentifiers = dataset.getRecordIdentifiers();
    }

    /**
     * [Test] Submits a GetRecords request with no search criteria. The
     * <code>namespace</code> parameter declares a namespace binding for the
     * common type name (tns:Record). The results must contain one or more
     * csw:BriefRecord elements.
     *
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>OGC 12-176r6, Table 19: KVP encoding for GetRecords operation
     * request</li>
     * <li>OGC 12-176r6, 7.3.4.1: NAMESPACE parameter</li>
     * </ul>
     */
    @Test(description = "Requirements: 063")
    public void getBriefRecordsWithNamespaceBinding() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.NAMESPACE, String.format("xmlns(tns=%s)", Namespaces.CSW));
        qryParams.put(CAT3.TYPE_NAMES, "tns:Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_BRIEF);
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        String expr = "count(//csw:SearchResults/csw:BriefRecord) > 0";
        ETSAssert.assertXPath(expr, entity, null);
    }

    /**
     * [Test] Submits a GetRecords request with no search criteria. The default
     * record representation is csw:SummaryRecord; the csw:SearchResults element
     * in the response cannot be empty.
     */
    @Test(description = "Requirements: 101")
    public void getSummaryRecordsInDefaultRepresentation() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        String expr = "count(//csw:SearchResults/csw:SummaryRecord) > 0";
        ETSAssert.assertXPath(expr, entity, null);
    }

    /**
     * [Test] Submits a GetRecords request with no search criteria. The Accept
     * header and the outputFormat parameter express a preference for an Atom
     * feed in the response. The resulting atom:feed must be valid according to
     * RFC 4287.
     *
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>OGC 12-176r6, 7.3.6: Atom response</li>
     * <li>OGC 10-032r8, Table 6: Elements of Search operation response in the
     * atom:feed element describing the search service</li>
     * <li><a href="https://tools.ietf.org/html/rfc4287" target="_blank">RFC
     * 4287</a>: The Atom Syndication Format</li>
     * </ul>
     */
    @Test(description = "Requirements: 80,122")
    public void getSummaryRecordsInAtomFeed() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.OUTPUT_FORMAT, MediaType.APPLICATION_ATOM_XML);
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_ATOM_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Assert.assertEquals(ClientUtils.removeParameters(response.getType()),
                MediaType.APPLICATION_ATOM_XML_TYPE,
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_MEDIA_TYPE));
        Document entity = getResponseEntityAsDocument(response, null);
        Map<String, String> nsBindings = Collections.singletonMap(Namespaces.ATOM, "atom");
        String expr = "count(/atom:feed/atom:entry) > 0";
        ETSAssert.assertXPath(expr, entity, nsBindings);
        Validator atomValidator = this.atomSchema.newValidator();
        ValidationErrorHandler err = new ValidationErrorHandler();
        atomValidator.setErrorHandler(err);
        try {
            Source src = XMLUtils.toStreamSource(new DOMSource(entity));
            atomValidator.validate(src);
        } catch (SAXException | IOException ex) {
            Logger.getLogger(GetRecordByIdTests.class.getName()).log(
                    Level.WARNING, "Error attempting to validate Atom feed.", ex);
        }
        Assert.assertFalse(err.errorsDetected(),
                ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                        err.getErrorCount(), err.toString()));
    }

    /**
     * [Test] Submits a GetRecords request that specifies an unsupported output
     * format (<code>text/example</code>) as the value of the outputFormat
     * parameter. An exception report is expected in response with HTTP status
     * code 400 and exception code
     * "{@value org.opengis.cite.cat30.CAT3#INVALID_PARAM_VAL}".
     */
    @Test(description = "Requirements: 035,037,042")
    public void getRecordsInUnsupportedOutputFormat() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.OUTPUT_FORMAT, "text/example");
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.WILDCARD_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.INVALID_PARAM_VAL,
                CAT3.OUTPUT_FORMAT);
    }

    /**
     * [Test] Submits a GetRecords request that specifies an unsupported output
     * schema ("urn:uuid:6a29d2a8-9651-47a6-9b14-f05d2b5644f0"). An exception
     * report is expected in response with HTTP status code 400 and exception
     * code "{@value org.opengis.cite.cat30.CAT3#INVALID_PARAM_VAL}".
     */
    @Test(description = "Requirements: 035,037,042")
    public void getRecordsInUnsupportedSchema() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.OUTPUT_SCHEMA, "urn:uuid:6a29d2a8-9651-47a6-9b14-f05d2b5644f0");
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.INVALID_PARAM_VAL,
                CAT3.OUTPUT_SCHEMA);
    }

    /**
     * [Test] Submits a GetRecords request with a BBOX parameter that includes
     * an invalid CRS reference ("urn:ogc:def:crs:EPSG::0000"). An exception
     * report is expected in response with HTTP status code 400 and exception
     * code "{@value org.opengis.cite.cat30.CAT3#INVALID_PARAM_VAL}".
     *
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>OGC 12-176r6, Table 6: KVP encoding for query constraints</li>
     * <li>OGC 06-121r9, 10.2.3: Bounding box KVP encoding</li>
     * </ul>
     */
    @Test(description = "Requirements: 017")
    public void getRecordsByBBOXWithUnsupportedCRS() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_BRIEF);
        qryParams.put(CAT3.BBOX,
                "472944,5363287,492722,5455253,urn:ogc:def:crs:EPSG::0000");
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.INVALID_PARAM_VAL, CAT3.BBOX);
    }

    /**
     * [Test] Submits a GetRecords request with a BBOX parameter that includes a
     * supported CRS reference. The brief records in the response must all
     * contain an ows:BoundingBox (or ows:WGS84BoundingBox) element that
     * intersects the specified bounding box.
     *
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>OGC 12-176r6, Table 1: Conformance classes [Filter-FES-KVP]</li>
     * <li>OGC 12-176r6, Table 6: KVP encoding for query constraints</li>
     * <li>OGC 06-121r9, 10.2.3: Bounding box KVP encoding</li>
     * <li>OGC 09-026r2 (ISO 19143), A.7: Test cases for minimum spatial
     * filter</li>
     * </ul>
     */
    @Test(description = "Requirements: 017; Tests: 017")
    public void getBriefRecordsByBBOX() {
        if (null == this.geoExtent) {
            throw new SkipException("Could not determine extent of sample data.");
        }
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_BRIEF);
        Envelope bbox = this.geoExtent;
        qryParams.put(CAT3.BBOX, Extents.envelopeToString(bbox));
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        Source results = new DOMSource(entity);
        ETSAssert.assertEnvelopeIntersectsBoundingBoxes(bbox, results);
    }

    /**
     * [Test] Submits a GetRecords request with a 'bbox' parameter that uses the
     * default CRS ("urn:ogc:def:crs:OGC:1.3:CRS84"). The summary records in the
     * response must all contain an ows:BoundingBox (or ows:WGS84BoundingBox)
     * element that intersects the specified bounding box.
     *
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>OGC 12-176r6, Table 1: Conformance classes [Filter-FES-KVP]</li>
     * <li>OGC 12-176r6, Table 6: KVP encoding for query constraints</li>
     * <li>OGC 06-121r9, 10.2.3: Bounding box KVP encoding</li>
     * <li>OGC 09-026r2 (ISO 19143), A.7: Test cases for minimum spatial
     * filter</li>
     * </ul>
     */
    @Test(description = "Requirements: 017; Tests: 017")
    public void getSummaryRecordsByWGS84BBOX() {
        if (null == this.geoExtent) {
            throw new SkipException("Could not determine extent of sample data.");
        }
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_SUMMARY);
        Envelope bbox = this.geoExtent;
        try {
            if (!bbox.getCoordinateReferenceSystem().equals(
                    DefaultGeographicCRS.WGS84)) {
                bbox = new GeneralEnvelope(Envelopes.transform(bbox,
                        DefaultGeographicCRS.WGS84));
            }
        } catch (TransformException ex) {
            throw new RuntimeException("Failed to create WGS84 envelope.", ex);
        }
        qryParams.put(CAT3.BBOX, Extents.envelopeToString(bbox));
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        Source results = new DOMSource(entity);
        ETSAssert.assertEnvelopeIntersectsBoundingBoxes(bbox, results);
    }

    /**
     * [Test] Submits a GetRecords request with a 'recordIds' parameter that
     * contains a (comma-separated) list of two record identifiers. Two matching
     * records (csw:SummaryRecord) are expected in the response.
     */
    @Test(description = "OGC 12-176, Table 6 - Record search")
    public void getMultipleRecordsById() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_SUMMARY);
        List<String> idList = new ArrayList<>();
        // select first and last identifiers
        idList.add(this.recordIdentifiers.get(0));
        StringBuilder paramValue = new StringBuilder();
        paramValue.append(idList.get(0)).append(',');
        idList.add(this.recordIdentifiers.get(this.recordIdentifiers.size() - 1));
        paramValue.append(idList.get(1));
        qryParams.put(CAT3.REC_ID_LIST, paramValue.toString());
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        QName recordName = new QName(Namespaces.CSW, "SummaryRecord");
        NodeList recordList = entity.getElementsByTagNameNS(
                recordName.getNamespaceURI(), recordName.getLocalPart());
        Assert.assertEquals(recordList.getLength(), 2,
                ErrorMessage.format(ErrorMessageKeys.RESULT_SET_SIZE, recordName));
        for (int i = 0; i < recordList.getLength(); i++) {
            Element record = (Element) recordList.item(i);
            NodeList identifiers = record.getElementsByTagNameNS(Namespaces.DCMES, "identifier");
            List<Node> recIdList = XMLUtils.getNodeListAsList(identifiers);
            // retain common elements (intersection)
            recIdList.retainAll(idList);
            Assert.assertFalse(recIdList.isEmpty(),
                    ErrorMessage.format(ErrorMessageKeys.ID_NOT_FOUND,
                            Records.getRecordId(record)));
        }
    }

    /**
     * The <code>Filter-FES-KVP</code> conformance class requires support for
     * text searches using the 'q' query parameter. This test submits a
     * GetRecords request where the 'q' parameter value is a word that occurs in
     * at least one catalog record.
     *
     * <p>
     * <strong>Note: </strong>According to Table 4 in <em>OGC OpenSearch Geo and
     * Time Extensions</em> (OGC 10-032r8), the domain of keyword matching
     * should include the record elements indicated below.</p>
     *
     * <table border="1" style="border-collapse: collapse;">
     * <thead>
     * <tr>
     * <th>csw:Record</th>
     * <th>atom:entry</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>
     * <ul>
     * <li>dc:title</li>
     * <li>dc:description (dct:abstract)</li>
     * <li>dc:subject</li>
     * </ul>
     * </td>
     * <td>
     * <ul>
     * <li>atom:title</li>
     * <li>atom:summary</li>
     * <li>atom:category</li>
     * </ul>
     * </td>
     * </tr>
     * </tbody>
     * </table>
     *
     * <h6 style="margin-bottom: 0.5em">Sources</h6>
     * <ul>
     * <li>OGC 12-176r6, Table 1: Conformance classes [Filter-FES-KVP]</li>
     * <li>OGC 12-176r6, Table 6: KVP encoding for query constraints</li>
     * <li>OGC 10-032r8, Table 4: Search operation queryable mappings</li>
     * </ul>
     */
    @Test(description = "OGC 12-176, Table 6 - Text search")
    public void basicTextSearch() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_FULL);
        int randomIndex = ThreadLocalRandom.current().nextInt(this.recordTitles.size());
        String[] titleWords = this.recordTitles.get(randomIndex).split("\\s+");
        String keyword = titleWords[titleWords.length - 1];
        qryParams.put(CAT3.Q, keyword);
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        QName recordName = new QName(Namespaces.CSW, "Record");
        NodeList recordList = entity.getElementsByTagNameNS(
                recordName.getNamespaceURI(), recordName.getLocalPart());
        Assert.assertTrue(recordList.getLength() > 0,
                ErrorMessage.format(ErrorMessageKeys.EMPTY_RESULT_SET, recordName));
        ETSAssert.assertTextOccurs(keyword, recordList);
    }

    /**
     * [Test] Submits a GetRecords request where the 'q' parameter value is
     * randomly generated text. The result set is expected to be empty.
     */
    @Test(description = "OGC 12-176, Table 6 - Text search")
    public void textSearchProducesEmptyResultSet() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_FULL);
        qryParams.put(CAT3.Q, Records.generateRandomText());
        request = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = getResponseEntityAsDocument(response, null);
        ETSAssert.assertEmptyResultSet(entity);
    }
}
