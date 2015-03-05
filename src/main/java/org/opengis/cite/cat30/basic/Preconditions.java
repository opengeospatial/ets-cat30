package org.opengis.cite.cat30.basic;

import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.util.CSWClient;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.testng.SkipException;
import org.testng.annotations.BeforeSuite;

/**
 * Checks that various preconditions are satisfied before the test suite is run.
 * If any of these (BeforeSuite) checks fail, all tests are skipped.
 */
public class Preconditions {

    /**
     * Verifies that a service capabilities document was supplied as a test run
     * argument and that the implementation it describes is available.
     *
     * @param testContext Information about the (pending) test run.
     */
    @BeforeSuite
    public void verifyTestSubject(ITestContext testContext) {
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
     * resulting File object is stored as the value of the suite attribute
     * {@link SuiteAttribute#DATA_FILE dataFile}.
     *
     * <p>
     * The resulting (full) record representations are inspected in order to
     * construct successful service requests (e.g. a GetRecordById request that
     * produces a matching record, GetRecords request with a spatial filter).
     * </p>
     *
     * @param testContext Information about the (pending) test run.
     */
    @BeforeSuite
    public void fetchSampleRecords(ITestContext testContext) {
        Document capabilitiesDoc = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        CSWClient cswClient = new CSWClient();
        cswClient.setServiceCapabilities(capabilitiesDoc);
        File dataFile = cswClient.saveFullRecords(20,
                MediaType.APPLICATION_XML_TYPE);
        if (!dataFile.isFile()) {
            throw new SkipException(
                    "Failed to save GetRecords response to temp file.");
        }
        /* TODO: uncomment block when have actual implementation
         QName docElemName = XMLUtils.nameOfDocumentElement(new StreamSource(dataFile));
         if (!docElemName.getLocalPart().equals("GetRecordsResponse")) {
         throw new SkipException(
         "Did not receive GetRecords response: " + docElemName);
         }
         */
        TestSuiteLogger.log(Level.INFO,
                "fetchSampleRecords: Saved GetRecords response to file: "
                + dataFile.getAbsolutePath());
        testContext.getSuite().setAttribute(
                SuiteAttribute.DATA_FILE.getName(), dataFile);
    }
}
