package org.opengis.cite.cat30;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Verifies the results of executing a test run using the main controller
 * (TestNGController).
 *
 */
public class VerifyTestNGController {

    private static DocumentBuilder docBuilder;
    private Properties testRunProps;

    @BeforeClass
    public static void initParser() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        dbf.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Before
    public void loadDefaultTestRunProperties()
            throws InvalidPropertiesFormatException, IOException {
        this.testRunProps = new Properties();
        this.testRunProps.loadFromXML(getClass().getResourceAsStream(
                "/test-run-props.xml"));
    }

    @Test
    public void skipAllTests_sutIsUnavailable() throws Exception {
        URL testSubject = getClass().getResource("basic-unavailable.xml");
        this.testRunProps.setProperty(TestRunArg.IUT.toString(), testSubject
                .toURI().toString());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
        this.testRunProps.storeToXML(outStream, "Integration test");
        Document testRunArgs = docBuilder.parse(new ByteArrayInputStream(
                outStream.toByteArray()));
        TestNGController controller = new TestNGController();
        Source source = controller.doTestRun(testRunArgs);
        Document resultsDoc = (Document) DOMSource.class.cast(source).getNode();
        Element docElem = resultsDoc.getDocumentElement();
        // all tests should have been skipped
        int nFailed = Integer.parseInt(docElem.getAttribute("failed"));
        assertTrue("Expected no failed verdicts.", nFailed == 0);
        int nPassed = Integer.parseInt(docElem.getAttribute("passed"));
        assertTrue("Expected no pass verdicts.", nPassed == 0);
    }
}
