package org.opengis.cite.cat30.basic;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URL;
import java.util.logging.Level;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.testng.annotations.BeforeSuite;

/**
 * <p>
 * Provides tests that apply to the <code>OGCWebService</code> interface defined
 * in the common service model (OGC 06-121r9, Figure C.2) as adapted for
 * Catalogue 3.0 implementations. The following service requests are covered:
 * </p>
 *
 * <ul>
 * <li>GetCapabilities: KVP syntax</li>
 * <li>GetRecordById: KVP syntax</li>
 * </ul>
 */
public class OGCWebServiceTests extends CommonFixture {

    private Document cswCapabilities;
    private Schema cswSchema;
    private static final String SCHEMATRON_CAPABILITIES
            = ROOT_PKG_PATH + "sch/csw-capabilities-3.0.sch";

    /**
     * Verifies that a service capabilities document was obtained and that the
     * implementation under test is available. If either condition is false all
     * tests will be marked as skipped.
     *
     * @param testContext Information about the pending test run.
     */
    @BeforeSuite
    public void verifyTestSubject(ITestContext testContext) {
        Object sutObj = testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        if (null != sutObj && Document.class.isInstance(sutObj)) {
            Document capabilitiesDoc = (Document) sutObj;
            String docElemNamespace = capabilitiesDoc.getDocumentElement().getNamespaceURI();
            Assert.assertEquals(docElemNamespace, Namespaces.CSW,
                    "Document element in unexpected namespace;");
            // TODO: Check service availability
        } else {
            String msg = String.format(
                    "Value of test suite attribute %s is missing or is not a DOM Document.",
                    SuiteAttribute.TEST_SUBJECT.getName());
            TestSuiteLogger.log(Level.SEVERE, msg);
            throw new AssertionError(msg);
        }
    }

    /**
     * Initializes the test fixture with the following items:
     *
     * <ul>
     * <li>CSW message schema (obtained from the suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#CSW_SCHEMA}, which should
     * evaluate to a thread-safe Schema object).</li>
     * <li>service capabilities document (obtained from the suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#TEST_SUBJECT}, which should
     * evaluate to a DOM Document node).</li>
     * </ul>
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void initOGCWebServiceTests(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJECT.getName());
        if (null == obj) {
            throw new SkipException("Capabilities document not found in ITestContext.");
        }
        this.cswCapabilities = Document.class.cast(obj);
        obj = testContext.getSuite().getAttribute(SuiteAttribute.CSW_SCHEMA.getName());
        if (null == obj) {
            throw new SkipException("CSW schema not found in ITestContext.");
        }
        this.cswSchema = Schema.class.cast(obj);
    }

    /**
     * Sets the service capabilities document. This method is intended to
     * facilitate unit testing.
     *
     * @param cswCapabilities A Document node representing a service description
     * (csw:Capabilities).
     */
    public void setServiceCapabilities(Document cswCapabilities) {
        this.cswCapabilities = cswCapabilities;
    }

    /**
     * [Test] Verifies that the content of a complete service capabilities
     * document is schema-valid. All implementations must support the GET method
     * for a GetCapabilities request.
     * <p>
     * The <code>Accept</code> request header expresses a preference for a
     * representation of type
     * {@value javax.ws.rs.core.MediaType#APPLICATION_XML}.
     * </p>
     */
    @Test(description = "Requirement-043,Requirement-045")
    public void getFullCapabilities_v3() {
        URI endpoint = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.ACCEPT_VERSIONS, CAT3.SPEC_VERSION);
        WebResource resource = this.client.resource(endpoint).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Validator validator = this.cswSchema.newValidator();
        Source source = new DOMSource(rsp.getEntity(Document.class));
        ETSAssert.assertSchemaValid(validator, source);
        URL schemaUrl = getClass().getResource(SCHEMATRON_CAPABILITIES);
        ETSAssert.assertSchematronValid(schemaUrl, source);
    }

    /**
     * [Test] Verifies that a request for an unsupported version of a
     * capabilities document produces an exception report containing the
     * exception code "VersionNegotiationFailed".
     *
     * The status code must be 400 (Bad Request) and the response entity must be
     * an XML document having {http://www.opengis.net/ows/2.0}ExceptionReport as
     * the document element.
     *
     * @see "OGC 06-121r9: 7.3.2, 8.6"
     */
    @Test(description = "Requirement-036,Requirement-037,Requirement-042")
    public void getCapabilitiesForUnsupportedVersion() {
        URI endpoint = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.ACCEPT_VERSIONS, "9999.12.31");
        WebResource resource = this.client.resource(endpoint).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        String xpath = String.format("//ows:Exception[@exceptionCode = '%s']",
                CAT3.VER_NEGOTIATION_FAILED);
        ETSAssert.assertXPath(xpath, rsp.getEntity(Document.class), null);
    }

    /**
     * [Test] Verifies that a request for a record by identifier produces a
     * response with status code 404 (Not Found) if no matching resource is
     * found. A response entity (an exception report) is optional; if present,
     * the exception code shall be "InvalidParameterValue".
     *
     * @see "OGC 06-121r9: 9.3.3.2"
     */
    @Test(description = "Requirement-127,Requirement-141")
    public void getRecordById_noMatchingRecord() {
        URI endpoint = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_RECORD_BY_ID);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.add(CAT3.ID, "urn:example:" + System.currentTimeMillis());
        WebResource resource = this.client.resource(endpoint).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.NOT_FOUND.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
    }
}
