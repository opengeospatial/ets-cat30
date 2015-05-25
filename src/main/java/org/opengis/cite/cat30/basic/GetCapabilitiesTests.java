package org.opengis.cite.cat30.basic;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.validation.Validator;

import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.ClientResponse;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.cat30.util.XMLUtils;

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
     * Finds the GET method endpoint for the GetCapabilities request in the
     * capabilities document.
     */
    @BeforeClass
    public void findServiceEndpoint() {
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
    public void getFullCapabilitiesAcceptVersion3() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.ACCEPT_VERSIONS, CAT3.VERSION_3_0_0);
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                qryParams, MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Source source = ClientUtils.getResponseEntityAsSource(response, null);
        Validator validator = this.cswSchema.newValidator();
        ETSAssert.assertSchemaValid(validator, source);
        URL schemaUrl = getClass().getResource(SCHEMATRON_CSW_CAPABILITIES);
        ETSAssert.assertSchematronValid(schemaUrl, source);
    }

    /**
     * [Test] Attempts to retrieve the capabilities document from the base URL
     * (endpoint for GetCapabilities via GET). The Accept header indicates that
     * any media type is acceptable ("&#42;/&#42;"); this is equivalent to
     * omitting the Accept header. The response shall include a complete XML
     * representation.
     *
     * @see "OGC 12-176r6, 6.4: Obtaining service metadata"
     */
    @Test(description = "Requirements: 006")
    public void getCapabilitiesFromBaseURL() {
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI, null,
                MediaType.WILDCARD_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Source source = ClientUtils.getResponseEntityAsSource(response, null);
        URL schURL = getClass().getResource(SCHEMATRON_CSW_CAPABILITIES);
        ETSAssert.assertSchematronValid(schURL, source);
    }

    /**
     * [Test] Attempts to retrieve the capabilities document from the base URL
     * (endpoint for GetCapabilities via GET). The Accept header indicates XML
     * as the preferred media type:
     *
     * <pre>Accept: text/html; q=0.5, application/xml</pre>
     *
     * The response shall include a complete XML representation.
     *
     * @see "OGC 12-176r6, 6.4:	Obtaining service metadata"
     */
    @Test(description = "Requirements: 007")
    public void getCapabilitiesFromBaseURLAsXML() {
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI, null,
                MediaType.valueOf(MediaType.TEXT_HTML + "; q=0.5"),
                MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Assert.assertEquals(response.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Source source = ClientUtils.getResponseEntityAsSource(response, null);
        URL schURL = getClass().getResource(SCHEMATRON_CSW_CAPABILITIES);
        ETSAssert.assertSchematronValid(schURL, source);
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
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put("Request", CAT3.GET_CAPABILITIES);
        qryParams.put("SERVICE", CAT3.SERVICE_TYPE_CODE);
        qryParams.put("acceptversions", CAT3.VERSION_3_0_0);
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                qryParams, MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        Document doc = ClientUtils.getResponseEntityAsDocument(response, null);
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
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, "getCapabilities");
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.ACCEPT_VERSIONS, CAT3.VERSION_3_0_0);
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                qryParams, MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.INVALID_PARAM_VAL, CAT3.REQUEST);
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
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.put(CAT3.ACCEPT_VERSIONS, CAT3.VERSION_3_0_0);
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                qryParams, MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.MISSING_PARAM_VAL, CAT3.SERVICE);
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
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.ACCEPT_VERSIONS, "9999.12.31");
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                qryParams, MediaType.APPLICATION_XML_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.VER_NEGOTIATION_FAILED,
                CAT3.ACCEPT_VERSIONS);
    }

    /**
     * [Test] Verifies that a request for a known but unsupported representation
     * (format) of a capabilities document produces an exception report
     * containing the exception code "InvalidParameterValue".
     *
     * The status code must be 400 (Bad Request) and the response entity must be
     * an XML document having {http://www.opengis.net/ows/2.0}ExceptionReport as
     * the document element.
     *
     * @see "OGC 06-121r9, Table 5: GetCapabilities operation request URL
     * parameters"
     */
    @Test(description = "Requirements: 036,037,042")
    public void getCapabilitiesInUnsupportedFormat() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.ACCEPT_VERSIONS, CAT3.VERSION_3_0_0);
        qryParams.put(CAT3.ACCEPT_FORMATS, "model/x3d+xml");
        request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                qryParams, MediaType.WILDCARD_TYPE);
        response = this.client.handle(request);
        ETSAssert.assertExceptionReport(response, CAT3.INVALID_PARAM_VAL,
                CAT3.ACCEPT_FORMATS);
    }

    /**
     * [Test] Verifies that a request for a supported representation (format) of
     * a capabilities document produces the expected response entity. All
     * supported formats must be listed in the capabilities document as values
     * of the "AcceptFormats" parameter. The media type "text/xml" must be
     * supported, but it need not be explicitly listed.
     *
     * @see "OGC 06-121r9, 7.3.5: AcceptFormats parameter"
     * @see "OGC 12-176, Table 15"
     */
    @Test(description = "Requirements: 036,037,042")
    public void getCapabilitiesInSupportedFormat() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.ACCEPT_VERSIONS, CAT3.VERSION_3_0_0);
        Set<String> allowedFormats = ServiceMetadataUtils.getParameterValues(
                cswCapabilities, CAT3.GET_CAPABILITIES, CAT3.ACCEPT_FORMATS);
        allowedFormats.add(MediaType.TEXT_XML);
        for (String format : allowedFormats) {
            qryParams.put(CAT3.ACCEPT_FORMATS, format);
            request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                    qryParams, MediaType.WILDCARD_TYPE);
            response = this.client.handle(request);
            Assert.assertEquals(response.getStatus(),
                    ClientResponse.Status.OK.getStatusCode(),
                    ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
            // ignore media type parameters
            MediaType mediaType = new MediaType(response.getType().getType(),
                    response.getType().getSubtype());
            Assert.assertEquals(mediaType,
                    MediaType.valueOf(format),
                    ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_MEDIA_TYPE));
        }
    }

    /**
     * [Test] Verifies that a request for a part of a capabilities document
     * produces the expected response entity. All recognized section names must
     * be listed in the capabilities document as values of the "Sections"
     * parameter; the value "All" indicates that a complete document is
     * requested.
     *
     * <p>
     * The possible section names are listed below.</p>
     * <ul>
     * <li>All</li>
     * <li>ServiceIdentification</li>
     * <li>ServiceProvider</li>
     * <li>OperationsMetadata</li>
     * <li>Filter_Capabilities</li>
     * </ul>
     *
     * @see "OGC 06-121r9, 7.3.3: Sections parameter"
     * @see "OGC 12-176, Table 12"
     */
    @Test(description = "Requirements: 044")
    public void getCapabilitiesBySection() {
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_CAPABILITIES);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.ACCEPT_VERSIONS, CAT3.VERSION_3_0_0);
        Set<String> sections = ServiceMetadataUtils.getParameterValues(
                cswCapabilities, CAT3.GET_CAPABILITIES, CAT3.SECTIONS);
        sections.add("All");
        for (String section : sections) {
            qryParams.put(CAT3.SECTIONS, section);
            request = ClientUtils.buildGetRequest(this.getCapabilitiesURI,
                    qryParams, MediaType.APPLICATION_XML_TYPE);
            response = this.client.handle(request);
            Assert.assertEquals(response.getStatus(),
                    ClientResponse.Status.OK.getStatusCode(),
                    ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
            Source source = ClientUtils.getResponseEntityAsSource(response, null);
            if (section.equals("All")) {
                URL schemaUrl = getClass().getResource(SCHEMATRON_CSW_CAPABILITIES);
                ETSAssert.assertSchematronValid(schemaUrl, source);
            } else { // check that only requested section appears
                String xpath = String.format(
                        "count(/csw:Capabilities/*) = count(//*[local-name()='%s'])",
                        section);
                try {
                    Boolean result = (Boolean) XMLUtils.evaluateXPath(
                            source, xpath, null, XPathConstants.BOOLEAN);
                    Assert.assertTrue(result, ErrorMessage.format(
                            ErrorMessageKeys.XPATH_ERROR, xpath));
                } catch (XPathExpressionException ex) {
                    TestSuiteLogger.log(Level.WARNING, ex.getMessage());
                }
            }
        }
    }
}
