package org.opengis.cite.cat30;

import com.sun.jersey.api.client.Client;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;

/**
 * A supporting base class that sets up a common test fixture. The configuration
 * methods are invoked before those defined in a subclass.
 */
public class CommonFixture {

    /**
     * Root test suite package (absolute path).
     */
    protected static final String ROOT_PKG_PATH = "/org/opengis/cite/cat30/";
    /**
     * HTTP client component (JAX-RS Client API).
     */
    protected Client client;

    /**
     * Initializes the common test fixture with a client component for
     * interacting with HTTP endpoints.
     *
     * @param testContext The test context that contains all the information for
     * a test run, including suite attributes.
     */
    @BeforeClass
    public void initCommonFixture(ITestContext testContext) {
        Object clientObj = testContext.getSuite().getAttribute(SuiteAttribute.CLIENT.getName());
        if (null != clientObj) {
            this.client = Client.class.cast(clientObj);
        }
    }
}
