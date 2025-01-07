package org.opengis.cite.cat30.basic;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ClientUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jakarta.ws.rs.core.MediaType;

public class VerifyPreconditions {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();

	private static DocumentBuilder docBuilder;

	private static ITestContext testContext;

	private static ISuite suite;

	@BeforeClass
	public static void initCommonFixture() throws Exception {
		testContext = mock(ITestContext.class);
		suite = mock(ISuite.class);
		when(testContext.getSuite()).thenReturn(suite);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void verifyTestSubjectIsNotCapabilitiesDoc() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Document element in unexpected namespace");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/atom/feed.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		try (MockedStatic<Reporter> reporter = Mockito.mockStatic(Reporter.class)) {
			ITestResult testResult = mock(ITestResult.class);
			reporter.when(() -> Reporter.getCurrentTestResult()).thenReturn(testResult);
			when(testResult.getTestContext()).thenReturn(testContext);
			SuitePreconditions iut = new SuitePreconditions();
			iut.verifyTestSubject();
		}
	}

}
