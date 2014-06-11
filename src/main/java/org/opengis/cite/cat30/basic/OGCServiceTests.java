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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * <p>
 * Provides tests that apply to the <code>OGC_Service</code> interface defined
 * in the common model as adapted for HTTP-based catalogue implementations. The
 * following service requests are covered:
 * </p>
 * 
 * <ul>
 * <li>GetCapabilities: KVP syntax</li>
 * <li>GetRecordById: KVP syntax</li>
 * </ul>
 */
public class OGCServiceTests {

    private Document cswCapabilities;
    private Schema cswSchema;
    private Client httpClient;

    /**
     * Initializes the test fixture with the following items:
     * 
     * <ul>
     * <li>an HTTP client component.</li>
     * <li>the CSW message schema (obtained from the suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#CSW_SCHEMA}, which should
     * evaluate to a thread-safe Schema object).</li>
     * <li>the service capabilities document (obtained from the suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#TEST_SUBJECT}, which should
     * evaluate to a DOM Document node).</li>
     * </ul>
     * 
     * <p>
     * The request and response messages may be logged to a default JDK logger
     * (in the namespace "com.sun.jersey.api.client").
     * </p>
     * 
     * @param testContext
     *            The test (group) context.
     */
    @BeforeClass
    public void initOGCServiceTests(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        if ((null != obj) && Document.class.isAssignableFrom(obj.getClass())) {
            this.cswCapabilities = Document.class.cast(obj);
        } else {
            throw new SkipException(
                    "Service capabilities not found in ITestContext.");
        }
        ClientConfig config = new DefaultClientConfig();
        this.httpClient = Client.create(config);
        this.httpClient.addFilter(new LoggingFilter());
        this.cswSchema = (Schema) testContext.getSuite().getAttribute(
                SuiteAttribute.CSW_SCHEMA.getName());
    }

    /**
     * Sets the service capabilities document. This method is intended to
     * facilitate unit testing.
     * 
     * @param serviceDescription
     *            A Document node representing a service description
     *            (csw:Capabilities).
     */
    public void setServiceCapabilities(Document serviceDescription) {
        this.cswCapabilities = serviceDescription;
    }

    /**
     * [{@code Test}] Verifies that the complete service capabilities document
     * is schema-valid. All catalogue services must support the GET method for a
     * GetCapabilities request.
     * 
     * @param testContext
     *            The test context containing the
     *            {@link SuiteAttribute#CSW_SCHEMA} attribute containing the
     *            complete CSW grammar.
     */
    @Test(description = "Requirement-043")
    public void getFullCapabilities(ITestContext testContext) {
        URI endpoint = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add("request", CAT3.GET_CAPABILITIES);
        qryParams.add("service", CAT3.SERVICE_TYPE_CODE);
        qryParams.add("version", CAT3.VERSION);
        WebResource resource = httpClient.resource(endpoint);
        resource.accept(MediaType.TEXT_XML_TYPE);
        ClientResponse rsp = resource.queryParams(qryParams).get(
                ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.OK.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
        Validator validator = this.cswSchema.newValidator();
        Source source = new DOMSource(rsp.getEntity(Document.class));
        ETSAssert.assertSchemaValid(validator, source);
    }
}
