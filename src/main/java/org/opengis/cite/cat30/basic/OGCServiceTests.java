package org.opengis.cite.cat30.basic;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

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

    /**
     * Obtains the service capabilities document from the ISuite context. The
     * suite attribute
     * {@link org.opengis.cite.cat30.SuiteAttribute#TEST_SUBJECT} should
     * evaluate to a DOM Document node.
     * 
     * @param testContext
     *            The test (group) context.
     */
    @BeforeClass
    public void obtainServiceCapabilities(ITestContext testContext) {
        Object obj = testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        if ((null != obj) && Document.class.isAssignableFrom(obj.getClass())) {
            this.cswCapabilities = Document.class.cast(obj);
        } else {
            throw new SkipException(
                    "Service capabilities not found in ITestContext.");
        }
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
     * is schema-valid.
     * 
     * @param testContext
     *            The test context containing the
     *            {@link SuiteAttribute#CSW_SCHEMA} attribute containing the
     *            complete CSW grammar.
     */
    @Test(description = "Requirement-076")
    public void getFullCapabilities(ITestContext testContext) {
        Schema cswSchema = (Schema) testContext.getSuite().getAttribute(
                SuiteAttribute.CSW_SCHEMA.getName());
        Validator validator = cswSchema.newValidator();
        Source source = new DOMSource(this.cswCapabilities);
        ETSAssert.assertSchemaValid(validator, source);
    }
}
