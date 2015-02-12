package org.opengis.cite.cat30.basic;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.testng.annotations.BeforeSuite;

/**
 * Provides tests pertaining to the <code>GetCapabilities</code> request. This
 * request implements the abstract <em>getCapabilities</em> operation defined in
 * the OGCWebService interface (OGC 06-121r9, Figure C.2).
 *
 * <p>
 * The KVP syntax must be supported; this encoding is generally used with the
 * GET method but may also be used with the POST method. The media type of a KVP
 * request entity is "application/x-www-form-urlencoded".
 * </p>
 *
 * <h6 style="margin-bottom: 0.5em">Sources</h6>
 * <ul>
 * <li>OGC 06-121r9, 7: GetCapabilities operation</li>
 * <li>OGC 12-176r6, 7.1: GetCapabilities operation</li>
 * </ul>
 */
public class GetCapabilitiesTests extends CommonFixture {

    private static final String SCHEMATRON_CSW_CAPABILITIES
            = ROOT_PKG_PATH + "sch/csw-capabilities-3.0.sch";
    /**
     * Service endpoint for GetCapabilities using the GET method.
     */
    private URI getCapabilitiesURI;

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
            URI getCapabilitiesGET = ServiceMetadataUtils.getOperationEndpoint(
                    capabilitiesDoc, CAT3.GET_CAPABILITIES, HttpMethod.GET);
            try {
                URL url = getCapabilitiesGET.toURL();
                URLConnection connection = url.openConnection();
                connection.connect();
            } catch (IOException iox) {
                throw new AssertionError("Service not available at "
                        + getCapabilitiesGET, iox);
            }
        } else {
            String msg = String.format(
                    "Value of test suite attribute %s is missing or is not a DOM Document.",
                    SuiteAttribute.TEST_SUBJECT.getName());
            TestSuiteLogger.log(Level.SEVERE, msg);
            throw new AssertionError(msg);
        }
    }

    /**
     * Finds the GET method endpoint for the GetCapabilities request in the
     * capabilities document.
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void findServiceEndpoint(ITestContext testContext) {
        this.getCapabilitiesURI = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
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
     *
     * @see "OGC 06-121r9, 7.2.1: GetCapabilities request parameters"
     */
    @Test(description = "Requirements: 043,045")
    public void getFullCapabilities_v3() {
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.ACCEPT_VERSIONS, CAT3.SPEC_VERSION);
        WebResource resource = this.client.resource(
                this.getCapabilitiesURI).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Source source = new DOMSource(rsp.getEntity(Document.class));
        Validator validator = this.cswSchema.newValidator();
        ETSAssert.assertSchemaValid(validator, source);
        URL schemaUrl = getClass().getResource(SCHEMATRON_CSW_CAPABILITIES);
        ETSAssert.assertSchematronValid(schemaUrl, source);
    }

    /**
     * [Test] Query parameter names must be handled in a case-insensitive
     * manner. The parameter names are all presented in mixed case; a complete
     * capabilities document is expected in response.
     *
     * @see "OGC 12-176r5, 6.5.4: KVP encoding rules"
     */
    @Test(description = "Requirements: 011")
    public void getCapabilitiesWithMixedCaseParamNames() {
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add("Request", CAT3.GET_CAPABILITIES);
        qryParams.add("SERVICE", CAT3.SERVICE_TYPE_CODE);
        qryParams.add("acceptversions", CAT3.SPEC_VERSION);
        WebResource resource = this.client.resource(
                this.getCapabilitiesURI).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Document doc = rsp.getEntity(Document.class);
        QName qName = new QName(Namespaces.CSW, "Capabilities");
        ETSAssert.assertQualifiedName(doc.getDocumentElement(), qName);
    }

    /**
     * [Test] Query parameter values must be handled in a case-sensitive manner.
     * The request specifies <code>request=getCapabilities</code>; an exception
     * report is expected in response with OGC exception code
     * {@value org.opengis.cite.cat30.CAT3#INVALID_PARAM_VAL} and status code
     * 400.
     *
     * @see "OGC 12-176r5, 6.5.4: KVP encoding rules"
     * @see "OGC 06-121r9, Table 28: Standard exception codes and meanings"
     */
    @Test(description = "Requirements: 012")
    public void getCapabilitiesWithInvalidParamValue() {
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, "getCapabilities");
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.ACCEPT_VERSIONS, CAT3.SPEC_VERSION);
        WebResource resource = this.client.resource(
                this.getCapabilitiesURI).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document doc = rsp.getEntity(Document.class);
        String xpath = String.format("//ows:Exception[@exceptionCode = '%s']",
                CAT3.INVALID_PARAM_VAL);
        ETSAssert.assertXPath(xpath, doc, null);
    }

    /**
     * [Test] If the required "service" parameter is missing, an exception
     * report with status code 400 must be produced. The expected OGC exception
     * code is {@value org.opengis.cite.cat30.CAT3#MISSING_PARAM_VAL}.
     *
     * @see "OGC 12-176r5, Table 5: KVP encoding of common operation request
     * parameters"
     */
    @Test(description = "Requirements: 010")
    public void getCapabilitiesIsMissingServiceParam() {
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.add(CAT3.ACCEPT_VERSIONS, CAT3.SPEC_VERSION);
        WebResource resource = this.client.resource(
                this.getCapabilitiesURI).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Document doc = rsp.getEntity(Document.class);
        String xpath = String.format("//ows:Exception[@exceptionCode = '%s']",
                CAT3.MISSING_PARAM_VAL);
        ETSAssert.assertXPath(xpath, doc, null);
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
     * @see "OGC 06-121r9, 7.3.2: Version negotiation"
     */
    @Test(description = "Requirements: 036,037,042")
    public void getCapabilitiesWithUnsupportedVersion() {
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.ACCEPT_VERSIONS, "9999.12.31");
        WebResource resource = this.client.resource(
                this.getCapabilitiesURI).queryParams(qryParams);
        Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        String xpath = String.format("//ows:Exception[@exceptionCode = '%s']",
                CAT3.VER_NEGOTIATION_FAILED);
        ETSAssert.assertXPath(xpath, rsp.getEntity(Document.class), null);
    }

}
