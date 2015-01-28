package org.opengis.cite.cat30;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.validation.Schema;
import org.opengis.cite.cat30.util.XMLUtils;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.cat30.util.URIUtils;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.w3c.dom.Document;

/**
 * A listener that performs various tasks before and after a test suite is run,
 * usually concerned with maintaining a shared test suite fixture. Since this
 * listener is loaded using the ServiceLoader mechanism, its methods will be
 * called before those of other suite listeners listed in the test suite
 * definition and before any annotated configuration methods.
 *
 * Attributes set on an ISuite instance are not inherited by constituent test
 * group contexts (ITestContext). However, suite attributes are still accessible
 * from lower contexts.
 *
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        processSuiteParameters(suite);
        buildSchema(suite);
        buildClientComponent(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
    }

    /**
     * Processes test suite arguments and sets suite attributes accordingly. The
     * entity referenced by the {@link TestRunArg#IUT iut} argument--expected to
     * be an OGC service capabilities document--is parsed and the resulting
     * Document is set as the value of the
     * {@link SuiteAttribute#TEST_SUBJECT testSubject} attribute.
     *
     * @param suite An ISuite object representing a TestNG test suite.
     */
    void processSuiteParameters(ISuite suite) {
        Map<String, String> params = suite.getXmlSuite().getParameters();
        TestSuiteLogger.log(Level.CONFIG,
                "Suite parameters\n" + params.toString());
        String iutParam = params.get(TestRunArg.IUT.toString());
        if ((null == iutParam) || iutParam.isEmpty()) {
            throw new IllegalArgumentException(
                    "Required test run parameter not found: "
                    + TestRunArg.IUT.toString());
        }
        URI iutRef = URI.create(iutParam.trim());
        File entityFile = null;
        try {
            entityFile = URIUtils.dereferenceURI(iutRef);
        } catch (IOException iox) {
            throw new RuntimeException("Failed to dereference resource located at "
                    + iutRef, iox);
        }
        Document iutDoc = null;
        try {
            iutDoc = URIUtils.parseURI(entityFile.toURI());
        } catch (Exception x) {
            throw new RuntimeException("Failed to parse resource retrieved from "
                    + iutRef, x);
        }
        suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), iutDoc);
        if (TestSuiteLogger.isLoggable(Level.FINE)) {
            StringBuilder logMsg = new StringBuilder(
                    "Parsed resource retrieved from ");
            logMsg.append(iutRef).append("\n");
            logMsg.append(XMLUtils.writeNodeToString(iutDoc));
            TestSuiteLogger.log(Level.FINE, logMsg.toString());
        }
    }

    /**
     * Builds a client component for interacting with HTTP endpoints. The client
     * will automatically redirect to the URI declared in 3xx responses. The
     * component is added to the suite fixture as the value of the
     * {@link SuiteAttribute#CLIENT} attribute; it may be subsequently accessed
     * via the {@link org.testng.ITestContext#getSuite()} method.
     *
     * <p>
     * The request and response messages may be logged to a default JDK logger
     * (in the namespace "com.sun.jersey.api.client").
     * </p>
     *
     * @param suite The test suite instance.
     */
    void buildClientComponent(ISuite suite) {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        Client client = Client.create(config);
        client.addFilter(new LoggingFilter());
        suite.setAttribute(SuiteAttribute.CLIENT.getName(), client);
    }

    /**
     * Builds a Schema object representing the complete set of XML Schema
     * constraints defined for CSW 3.0. The schema is added to the suite fixture
     * as the value of the {@link SuiteAttribute#CSW_SCHEMA} attribute.
     *
     * @param suite The test suite to be run.
     */
    void buildSchema(ISuite suite) {
        Schema csw3Schema = ValidationUtils.createCSWSchema();
        if (null != csw3Schema) {
            suite.setAttribute(SuiteAttribute.CSW_SCHEMA.getName(), csw3Schema);
        }
    }
}
