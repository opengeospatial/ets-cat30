package org.opengis.cite.cat30.basic;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Provides tests that apply to the <code>GetRecordById</code> request. This
 * request implements the abstract <em>GetResourceByID</em> operation defined in
 * the OGCWebService interface (OGC 06-121r9, Figure C.2).
 *
 * <p>
 * The KVP syntax must be supported; this encoding is generally used with the
 * GET method but may also be used with the POST method; this latter capability
 * will be advertised in the capabilities document as an operational constraint
 * as indicated below. The media type of a KVP request entity is
 * "application/x-www-form-urlencoded".
 * </p>
 *
 * <pre>{@literal
 *<Post xmlns="http://www.opengis.net/ows/2.0"
 *  xmlns:xlink="http://www.w3.org/1999/xlink"
 *  xlink:href="http://cat.example.org/csw">
 *  <Constraint name="PostEncoding">
 *    <AllowedValues>
 *      <Value>KVP</Value>
 *    </AllowedValues>
 *  </Constraint>
 *</Post>
 *}
 * </pre>
 *
 * @see "OGC 12-176r6, 7.4: GetRecordById operation"
 * @see "OGC 12-176r6, Table 16: Operation constraints"
 * @see "OGC 06-121r9, 7.4.7: OperationsMetadata section standard contents"
 */
public class GetRecordByIdTests extends CommonFixture {

    /**
     * Service endpoint for GetRecordById using the GET method.
     */
    private URI endpoint;

    /**
     * Finds the GET method endpoint for the GetCapabilities request in the
     * capabilities document.
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void findServiceEndpoint(ITestContext testContext) {
        this.endpoint = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_RECORD_BY_ID, HttpMethod.GET);
    }

    /**
     * [Test] Verifies that a request for a record by identifier produces a
     * response with status code 404 (Not Found) if no matching resource is
     * found. A response entity (an exception report) is optional; if present,
     * the exception code shall be "InvalidParameterValue".
     *
     * @see "OGC 06-121r9, 9.3.3.2"
     */
    @Test(description = "Requirement-127,Requirement-141")
    public void getRecordById_noMatchingRecord() {
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_RECORD_BY_ID);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.add(CAT3.ID, "urn:example:" + System.currentTimeMillis());
        WebResource resource = this.client.resource(this.endpoint).queryParams(qryParams);
        WebResource.Builder builder = resource.accept(MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = builder.get(ClientResponse.class);
        Assert.assertEquals(rsp.getStatus(),
                ClientResponse.Status.NOT_FOUND.getStatusCode(),
                ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
    }
}
