package org.opengis.cite.cat30.opensearch;

import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.SuiteAttribute;
import org.testng.ITestContext;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.util.CSWClient;
import org.opengis.cite.cat30.util.XMLUtils;
import org.testng.annotations.BeforeTest;

/**
 * Checks that various preconditions are satisfied before the tests for
 * OpenSearch conformance are run. If any of these checks fail, all applicable
 * tests are skipped.
 */
public class OpenSearchPreconditions {

    /**
     * Checks that the service capabilities document advertises OpenSearch
     * support. The implementation status of the corresponding conformance class
     * must be set to "TRUE".
     *
     * <pre>{@literal
     *<OperationsMetadata xmlns="http://www.opengis.net/ows/2.0">
     *  <!-- Operation elements omitted -->
     *  <Constraint name="http://www.opengis.net/spec/csw/3.0/conf/OpenSearch">
     *    <AllowedValues>
     *      <Value>FALSE</Value>
     *      <Value>TRUE</Value>
     *    </AllowedValues>
     *    <DefaultValue>TRUE</DefaultValue>
     *    <Meaning>Conformance class</Meaning>
     *  </Constraint>
     *</OperationsMetadata>
     *}
     * </pre>
     *
     * An attempt will be made to retrieve an OpenSearch description document
     * from the IUT. A representation is expected to be obtained by
     * dereferencing the URI corresponding to the GetCapabilities (GET)
     * endpoint. The resulting Document object is stored as the value of the
     * suite attribute {@link SuiteAttribute#OPENSEARCH_DESCR}.
     *
     * @param testContext Information about the current test run.
     *
     * @see "OGC 12-176r5, Table 17: Service constraints"
     */
    @BeforeTest
    public void checkOpenSearchImplementationStatus(ITestContext testContext) {
        Document cswCapabilities = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        String xpath = String.format(
                "//ows:Constraint[contains(@name,'%s')]/ows:DefaultValue = 'TRUE'",
                CAT3.CC_OPEN_SEARCH);
        Boolean isSupported = null;
        try {
            isSupported = (Boolean) XMLUtils.evaluateXPath(
                    new DOMSource(cswCapabilities), xpath, null,
                    XPathConstants.BOOLEAN);
        } catch (XPathExpressionException ex) { // ignore--expression ok
        }
        if (!isSupported) {
            throw new AssertionError(
                    "OpenSearch not supported. Expected: \n" + xpath);
        }
        CSWClient cswClient = new CSWClient();
        cswClient.setServiceDescription(cswCapabilities);
        Document openSearchDescr = cswClient.getOpenSearchDescription(null);
        if (null == openSearchDescr) {
            throw new AssertionError(ErrorMessage.get(
                    ErrorMessageKeys.OPENSEARCH_UNAVAIL));
        }
        testContext.getSuite().setAttribute(
                SuiteAttribute.OPENSEARCH_DESCR.getName(), openSearchDescr);
    }
}
