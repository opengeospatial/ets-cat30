package org.opengis.cite.cat30.basic;

import java.net.URI;
import javax.ws.rs.HttpMethod;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;

/**
 * Provides tests that apply to the <code>GetRecords</code> request using the
 * KVP syntax.
 *
 * <p>
 * The KVP syntax must be supported; this encoding is generally used with the
 * GET method but may also be used with the POST method; this latter capability
 * will be advertised in the capabilities document as an operational constraint
 * as indicated below. The media type of a KVP request entity is
 * "application/x-www-form-urlencoded".
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
        this.postURI = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_RECORDS, HttpMethod.POST);
    }
}
