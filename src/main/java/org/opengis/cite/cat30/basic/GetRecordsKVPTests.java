package org.opengis.cite.cat30.basic;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.opengis.cite.cat30.util.XMLUtils;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
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
     * [Test] Submits a GetRecords request with no search criteria. The default
     * record representation is csw:SummaryRecord; the csw:SearchResults element
     * cannot be empty.
     */
    @Test(description = "Requirements: 101")
    public void getSummaryRecordsInDefaultRepresentation() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.TYPE_NAMES, "Record");
        ClientRequest req = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = this.client.handle(req);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document entity = rsp.getEntity(Document.class);
        String expr = String.format("count(//csw:SearchResults/csw:SummaryRecord) > 0");
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
        ClientRequest req = ClientUtils.buildGetRequest(this.getURI, qryParams,
                MediaType.APPLICATION_ATOM_XML_TYPE);
        ClientResponse rsp = this.client.handle(req);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Assert.assertEquals(ClientUtils.removeParameters(rsp.getType()),
                MediaType.APPLICATION_ATOM_XML_TYPE,
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_MEDIA_TYPE));
        Document entity = rsp.getEntity(Document.class);
        Map<String, String> nsBindings = Collections.singletonMap(Namespaces.ATOM, "atom");
        String expr = String.format("count(/atom:feed/atom:entry) > 0");
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
}
