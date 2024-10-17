package org.opengis.cite.cat30;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.validation.Schema;

import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.DatasetInfo;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.cat30.util.URIUtils;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.opengis.cite.cat30.util.XMLUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jakarta.ws.rs.client.Client;

/**
 * A listener that performs various tasks before and after a test suite is run, usually
 * concerned with maintaining a shared test suite fixture. Since this listener is loaded
 * using the ServiceLoader mechanism, its methods will be called before those of other
 * suite listeners listed in the test suite definition and before any annotated
 * configuration methods.
 *
 * Attributes set on an ISuite instance are not inherited by constituent test group
 * contexts (ITestContext). However, suite attributes are still accessible from lower
 * contexts.
 *
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		processSuiteParameters(suite);
		registerSchemas(suite);
		registerClientComponent(suite);
	}

	/**
	 * Performs various cleanup tasks when the test run is completed. Any temporary files
	 * created during the test run are deleted if TestSuiteLogger is enabled at the INFO
	 * level or higher.
	 * @param suite The test suite.
	 */
	@Override
	public void onFinish(ISuite suite) {
		deleteTempFiles(suite);
	}

	/**
	 * Processes test suite arguments and sets suite attributes accordingly. The entity
	 * referenced by the {@link TestRunArg#IUT iut} argument--expected to be an OGC
	 * service capabilities document--is parsed and the resulting Document is set as the
	 * value of the {@link SuiteAttribute#TEST_SUBJECT testSubject} attribute.
	 * @param suite An ISuite object representing a TestNG test suite.
	 */
	void processSuiteParameters(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		TestSuiteLogger.log(Level.CONFIG, "Suite parameters\n" + params.toString());
		String iutParam = params.get(TestRunArg.IUT.toString());
		if ((null == iutParam) || iutParam.isEmpty()) {
			throw new IllegalArgumentException("Required test run parameter not found: " + TestRunArg.IUT.toString());
		}
		URI iutRef = URI.create(iutParam.trim());
		File entityFile = null;
		try {
			entityFile = URIUtils.dereferenceURI(iutRef);
		}
		catch (IOException iox) {
			throw new RuntimeException("Failed to dereference resource located at " + iutRef, iox);
		}
		suite.setAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName(), entityFile);
		Document iutDoc = null;
		try {
			iutDoc = URIUtils.parseURI(entityFile.toURI());
		}
		catch (SAXException | IOException x) {
			throw new RuntimeException("Failed to parse resource retrieved from " + iutRef, x);
		}
		suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), iutDoc);
		if (TestSuiteLogger.isLoggable(Level.FINE)) {
			StringBuilder logMsg = new StringBuilder("Parsed resource retrieved from ");
			logMsg.append(iutRef).append("\n");
			logMsg.append(XMLUtils.writeNodeToString(iutDoc));
			TestSuiteLogger.log(Level.FINE, logMsg.toString());
		}
	}

	/**
	 * A client component is added to the suite fixture as the value of the
	 * {@link SuiteAttribute#CLIENT} attribute; it may be subsequently accessed via the
	 * {@link org.testng.ITestContext#getSuite()} method.
	 * @param suite The test suite instance.
	 */
	void registerClientComponent(ISuite suite) {
		Client client = ClientUtils.buildClient();
		if (null != client) {
			suite.setAttribute(SuiteAttribute.CLIENT.getName(), client);
		}
	}

	/**
	 * Builds immutable {@link Schema Schema} objects suitable for validating the content
	 * of CSW 3.0 response entities. The schemas are added to the suite fixture as the
	 * value of the attributes identified in the following table.
	 *
	 * <table border="1" style="border-collapse: collapse;">
	 * <caption>Application schemas</caption> <thead>
	 * <tr>
	 * <th>SuiteAttribute</th>
	 * <th>Schema</th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <td><code>SuiteAttribute.CSW_SCHEMA</code></td>
	 * <td>OGC 12-176r6, 7.9(a): cswAll.xsd</td>
	 * </tr>
	 * <tr>
	 * <td><code>SuiteAttribute.ATOM_SCHEMA</code></td>
	 * <td>RFC 4287, Appendix B: RELAX NG Compact Schema</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * @param suite The test suite to be run.
	 */
	void registerSchemas(ISuite suite) {
		Schema csw3Schema = ValidationUtils.createCSWSchema();
		if (null != csw3Schema) {
			suite.setAttribute(SuiteAttribute.CSW_SCHEMA.getName(), csw3Schema);
		}
		Schema atomSchema = ValidationUtils.createAtomSchema();
		if (null != atomSchema) {
			suite.setAttribute(SuiteAttribute.ATOM_SCHEMA.getName(), atomSchema);
		}
	}

	/**
	 * Deletes temporary files created during the test run if TestSuiteLogger is enabled
	 * at the INFO level or higher (they are left intact at the CONFIG level or lower).
	 * @param suite The test suite.
	 */
	void deleteTempFiles(ISuite suite) {
		if (TestSuiteLogger.isLoggable(Level.CONFIG)) {
			return;
		}
		File testSubjFile = (File) suite.getAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName());
		if (testSubjFile.exists()) {
			testSubjFile.delete();
		}
		DatasetInfo dataset = (DatasetInfo) suite.getAttribute(SuiteAttribute.DATASET.getName());
		if (null != dataset) {
			File dataFile = dataset.getDataFile();
			dataFile.delete();
		}
	}

}
