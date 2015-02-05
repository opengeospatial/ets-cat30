package org.opengis.cite.cat30;

import com.sun.jersey.api.client.Client;
import javax.xml.validation.Schema;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;

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
     * Service capabilities document (csw:Capabilities)
     */
    protected Document cswCapabilities;
    /**
     * Complete CSW 3.0 application schema (cswAll.xsd)
     */
    protected Schema cswSchema;

    /**
     * Initializes the common test fixture with the following objects:
     *
     * <ul>
     * <li>a client component for interacting with HTTP endpoints</li>
     * <li>the CSW message schema (obtained from the suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#CSW_SCHEMA}, which should
     * evaluate to a thread-safe Schema object).</li>
     * <li>the service capabilities document (obtained from the suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#TEST_SUBJECT}, which should
     * evaluate to a DOM Document node).</li>
     * </ul>
     *
     * @param testContext The test context that contains all the information for
     * a test run, including suite attributes.
     */
    @BeforeClass
    public void initCommonFixture(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(SuiteAttribute.CLIENT.getName());
        if (null != obj) {
            this.client = Client.class.cast(obj);
        }
        obj = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJECT.getName());
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
}
