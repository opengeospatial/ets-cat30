package org.opengis.cite.cat30.opensearch;

import java.util.Collections;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.SuiteAttribute;
import org.testng.ITestContext;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.util.CSWClient;
import org.opengis.cite.cat30.util.XMLUtils;
import org.testng.annotations.BeforeTest;

/**
 * Checks that various preconditions are satisfied before the tests for
 * OpenSearch conformance are run. If any of these checks fail, all applicable
 * tests are skipped.
 */
public class OpenSearchPreconditions {

    private CSWClient cswClient;

    void setClient(CSWClient client) {
        this.cswClient = client;
    }

    public OpenSearchPreconditions() {
        this.cswClient = new CSWClient();
    }

    /**
     * Checks that the service capabilities document advertises OpenSearch
     * support. The implementation status of the corresponding conformance class
     * must be set to "TRUE". The ows:DefaultValue element takes precedence; if
     * it does not appear then the value of the first occurrence of the
     * ows:Value element will be checked.
     *
     * <pre>{@literal
     *<OperationsMetadata xmlns="http://www.opengis.net/ows/2.0">
     *  <!-- Operation elements omitted -->
     *  <Constraint name="http://www.opengis.net/spec/csw/3.0/conf/OpenSearch">
     *    <AllowedValues>
     *      <Value>TRUE</Value>
     *      <Value>FALSE</Value>
     *    </AllowedValues>
     *    <DefaultValue>TRUE</DefaultValue>
     *    <Meaning>Conformance class</Meaning>
     *  </Constraint>
     *</OperationsMetadata>
     *}
     * </pre>
     *
     * <p>
     * An attempt will be made to retrieve an OpenSearch description document
     * from the IUT. A representation is expected to be obtained by
     * dereferencing the URI corresponding to the GetCapabilities (GET)
     * endpoint. The resulting Document object is stored as the value of the
     * suite attribute {@link SuiteAttribute#OPENSEARCH_DESCR}.
     * </p>
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
                "//ows:Constraint[ends-with(@name,'%s')]/ows:DefaultValue",
                CAT3.CC_OPEN_SEARCH);
        XdmValue xdmValue = null;
        Map<String, String> nsMap = Collections.singletonMap(Namespaces.OWS, "ows");
        Source source = new DOMSource(cswCapabilities, cswCapabilities.getDocumentURI());
        try {
            xdmValue = XMLUtils.evaluateXPath2(source, xpath, nsMap);
            if (xdmValue.size() == 0) {
                xpath = String.format(
                        "//ows:Constraint[ends-with(@name,'%s')]//ows:Value[1]",
                        CAT3.CC_OPEN_SEARCH);
                xdmValue = XMLUtils.evaluateXPath2(source, xpath, nsMap);
            }
        } catch (SaxonApiException ex) { // ignore--expressions ok
        }
        if (xdmValue.size() == 0
                || !xdmValue.itemAt(0).getStringValue().trim().equalsIgnoreCase("TRUE")) {
            throw new AssertionError("OpenSearch not a supported capability.");
        }
        this.cswClient.setServiceDescription(cswCapabilities);
        Document openSearchDescr = this.cswClient.getOpenSearchDescription(null);
        if (null == openSearchDescr) {
            throw new AssertionError(ErrorMessage.get(
                    ErrorMessageKeys.OPENSEARCH_UNAVAIL));
        }
        testContext.getSuite().setAttribute(
                SuiteAttribute.OPENSEARCH_DESCR.getName(), openSearchDescr);
    }
}
