package org.opengis.cite.cat30.basic;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.CSWClient;
import org.opengis.cite.cat30.util.DatasetInfo;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.cat30.util.XMLUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.BeforeSuite;
import org.w3c.dom.Document;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MediaType;

/**
 * Checks that various preconditions are satisfied before the test suite is run.
 * If any of these (BeforeSuite) methods fail, all tests are skipped.
 */
public class SuitePreconditions {

    /**
     * Verifies that a service capabilities document was supplied as a test run
     * argument and that the implementation it describes is available.
     *
     * @param testContext Information about the test run.
     */
    @BeforeSuite
    public void verifyTestSubject() {
        ITestContext testContext = Reporter.getCurrentTestResult().getTestContext();
        Object sutObj = testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        if (null != sutObj && Document.class.isInstance(sutObj)) {
            Document capabilitiesDoc = (Document) sutObj;
            String docElemNamespace = capabilitiesDoc.getDocumentElement().getNamespaceURI();
            Assert.assertEquals(docElemNamespace, Namespaces.CSW,
                    "Document element in unexpected namespace;");
            URI getCapabilitiesGET = ServiceMetadataUtils.getOperationEndpoint(
                    capabilitiesDoc, CAT3.GET_CAPABILITIES, HttpMethod.GET);
            try {
                URL url = getCapabilitiesGET.toURL();
                URLConnection connection = url.openConnection();
                connection.connect();
            } catch (IOException iox) {
                throw new AssertionError("Service not available at "
                        + getCapabilitiesGET, iox);
            }
        } else {
            String msg = String.format(
                    "Value of test suite attribute %s is missing or is not a DOM Document.",
                    SuiteAttribute.TEST_SUBJECT.getName());
            TestSuiteLogger.log(Level.SEVERE, msg);
            throw new AssertionError(msg);
        }
    }

    /**
     * Fetches records from the IUT using a simple GetRecords request (with no
     * filter criteria) and saves the response entity to a temporary file. The
     * resulting {@link DatasetInfo DatasetInfo} object is stored as the value
     * of the suite attribute {@link SuiteAttribute#DATASET dataset}.
     *
     * <p>
     * The resulting csw:Record (full) representations are inspected in order to
     * construct successful service requests (e.g. a GetRecordById request that
     * produces a matching record, GetRecords request with a spatial filter).
     * </p>
     *
     * @param testContext Information about the test run.
     */
    @BeforeSuite
    public void fetchSampleData() {
        ITestContext testContext = Reporter.getCurrentTestResult().getTestContext();
        Document capabilitiesDoc = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        CSWClient cswClient = new CSWClient();
        cswClient.setServiceDescription(capabilitiesDoc);
        File dataFile = cswClient.saveFullRecords(20,
                MediaType.APPLICATION_XML_TYPE);
        if (!dataFile.isFile()) {
            throw new AssertionError(
                    "Failed to save GetRecords response to temp file.");
        }
        Boolean hasResults = null;
        try {
            hasResults = (Boolean) XMLUtils.evaluateXPath(
                    new StreamSource(dataFile), "//csw:Record", null, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException ex) {
            // not possible
        }
        if (!hasResults) {
            throw new AssertionError(
                    "fetchSampleData: No csw:Record elements found in GetRecords response.");
        }
        TestSuiteLogger.log(Level.INFO,
                "fetchSampleData: Saved GetRecords response to file: "
                + dataFile.getAbsolutePath());
        DatasetInfo dataset = new DatasetInfo(dataFile);
        testContext.getSuite().setAttribute(
                SuiteAttribute.DATASET.getName(), dataset);
    }
}
