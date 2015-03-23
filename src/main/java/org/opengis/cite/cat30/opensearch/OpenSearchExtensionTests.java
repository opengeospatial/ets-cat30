package org.opengis.cite.cat30.opensearch;

import org.opengis.cite.cat30.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;

/**
 * Verifies behavior of the SUT when processing queries that contain custom
 * parameters defined in <em>OGC OpenSearch Geo and Time Extensions</em> (OGC
 * 10-032r8).
 *
 * <p>
 * All implementations must satisfy the requirements of the
 * <strong>Core</strong> conformance class (see OGC 10-032r8, Table 1). A
 * conforming service must:</p>
 * <ul>
 * <li>present a valid OpenSearch description document;</li>
 * <li>define a URL template for the Atom response type;</li>
 * <li>implement a bounding box search (<code>geo:box</code>).</li>
 * </ul>
 *
 * <p>
 * In addition to the service capabilities listed above, a Catalog v3 service
 * must also implement the <strong>Get record by id</strong> conformance class
 * that allows a client to retrieve a record by identifier
 * (<code>geo:uid</code>).</p>
 *
 * @see
 * <a href="https://portal.opengeospatial.org/files/?artifact_id=56866&version=2"
 * target="_blank">OGC OpenSearch Geo and Time Extensions, Version 1.0.0</a>
 */
public class OpenSearchExtensionTests {

    private Document openSearchDescr;

    /**
     * Initializes the test fixture. A Document representing an OpenSearch
     * description document is obtained from the test context.
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void initFixture(ITestContext testContext) {
        this.openSearchDescr = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.OPENSEARCH_DESCR.getName());
    }

}
