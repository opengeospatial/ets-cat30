package org.opengis.cite.cat30.opensearch;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.CSWClient;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VerifyOpenSearchPreconditions {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static DocumentBuilder docBuilder;

	private static ITestContext testContext;

	private static ISuite suite;

	@BeforeClass
	public static void initTestFixture() throws Exception {
		testContext = mock(ITestContext.class);
		suite = mock(ISuite.class);
		when(testContext.getSuite()).thenReturn(suite);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void declareOpenSearchUsingDefaultValue() throws SAXException, IOException {
		CSWClient client = mock(CSWClient.class);
		when(client.getOpenSearchDescription(nullable(URI.class))).thenReturn(docBuilder.newDocument());
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities/basic.xml"));
		when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
		OpenSearchPreconditions iut = new OpenSearchPreconditions();
		iut.setClient(client);
		iut.checkOpenSearchImplementationStatus(testContext);
	}

	@Test
	public void declareOpenSearchUsingAllowedValue() throws SAXException, IOException {
		CSWClient client = mock(CSWClient.class);
		when(client.getOpenSearchDescription(nullable(URI.class))).thenReturn(docBuilder.newDocument());
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities/pycsw-cite.xml"));
		when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
		OpenSearchPreconditions iut = new OpenSearchPreconditions();
		iut.setClient(client);
		iut.checkOpenSearchImplementationStatus(testContext);
	}

}
