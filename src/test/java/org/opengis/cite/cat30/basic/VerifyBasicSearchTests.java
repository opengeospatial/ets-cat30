package org.opengis.cite.cat30.basic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.TestCommon;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.opengis.cite.cat30.util.XMLUtils;
import org.opengis.cite.geomatics.Extents;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class VerifyBasicSearchTests extends TestCommon {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static DocumentBuilder docBuilder;

	private static ITestContext testContext;

	private static Schema cswSchema;

	private static Schema atomSchema;

	@BeforeClass
	public static void initTestFixture() throws Exception {
		testContext = mock(ITestContext.class);
		when(testContext.getSuite()).thenReturn(suite);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
		Document doc = docBuilder.parse(VerifyBasicSearchTests.class.getResourceAsStream("/capabilities/basic.xml"));
		when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
		cswSchema = ValidationUtils.createCSWSchema();
		when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName())).thenReturn(cswSchema);
		atomSchema = ValidationUtils.createAtomSchema();
		when(suite.getAttribute(SuiteAttribute.ATOM_SCHEMA.getName())).thenReturn(atomSchema);
	}

	@Test
	public void getBriefRecordsByBBOX_allIntersect()
			throws SAXException, IOException, FactoryException, TransformException {
		mockResponse();
		Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream("/rsp/GetRecordsResponse-full.xml"));
		BasicSearchTests spy = Mockito.spy(new BasicSearchTests());
		try (MockedStatic<ClientUtils> clientUtils = Mockito.mockStatic(ClientUtils.class)) {
			clientUtils.when(() -> ClientUtils.buildGetRequest(any(URI.class), any(Map.class), any(MediaType.class)))
				.thenReturn(rsp);
			Mockito.doReturn(rspEntity)
				.when(spy)
				.getResponseEntityAsDocument(any(Response.class), nullable(String.class));
			spy.initCommonFixture(testContext);
			// BOX2D(32.5 -117.6, 34 -115) with CRS EPSG:4326
			spy.setExtent(buildEnvelope(1));
			spy.setGetEndpoint(URI.create("http://localhost/csw/v3"));
			spy.getBriefRecordsByBBOX();
		}
	}

	@Test
	public void getBriefRecordsByBBOX_noneIntersect()
			throws SAXException, IOException, FactoryException, TransformException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("The envelopes do not intersect");
		mockResponse();
		Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream("/rsp/GetRecordsResponse-full.xml"));
		BasicSearchTests spy = Mockito.spy(new BasicSearchTests());
		try (MockedStatic<ClientUtils> clientUtils = Mockito.mockStatic(ClientUtils.class)) {
			clientUtils.when(() -> ClientUtils.buildGetRequest(any(URI.class), any(Map.class), any(MediaType.class)))
				.thenReturn(rsp);
			Mockito.doReturn(rspEntity)
				.when(spy)
				.getResponseEntityAsDocument(any(Response.class), nullable(String.class));
			spy.initCommonFixture(testContext);
			// BOX2D(472944 5363287, 516011 5456383) with CRS EPSG:32610
			spy.setExtent(buildEnvelope(2));
			spy.setGetEndpoint(URI.create("http://localhost/csw/v3"));
			spy.getBriefRecordsByBBOX();
		}
	}

	@Test
	public void getSummaryRecordsByWGS84BBOX_noneIntersect()
			throws SAXException, IOException, FactoryException, TransformException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("The envelopes do not intersect");
		mockResponse();
		Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream("/rsp/GetRecordsResponse-full.xml"));
		BasicSearchTests spy = Mockito.spy(new BasicSearchTests());
		try (MockedStatic<ClientUtils> clientUtils = Mockito.mockStatic(ClientUtils.class)) {
			clientUtils.when(() -> ClientUtils.buildGetRequest(any(URI.class), any(Map.class), any(MediaType.class)))
				.thenReturn(rsp);
			Mockito.doReturn(rspEntity)
				.when(spy)
				.getResponseEntityAsDocument(any(Response.class), nullable(String.class));
			spy.initCommonFixture(testContext);
			// BOX2D(472944 5363287, 516011 5456383) with CRS EPSG:32610
			spy.setExtent(buildEnvelope(2));
			spy.setGetEndpoint(URI.create("http://localhost/csw/v3"));
			spy.getSummaryRecordsByWGS84BBOX();
		}
	}

	private Envelope buildEnvelope(int id) throws SAXException, IOException, FactoryException, TransformException {
		String path = String.format("/rsp/GetRecordsResponse-%d.xml", id);
		Document doc = docBuilder.parse(getClass().getResourceAsStream(path));
		NodeList boxNodes = null;
		try {
			boxNodes = XMLUtils.evaluateXPath(doc,
					"//csw:Record/ows:BoundingBox[1] | //csw:Record/ows:WGS84BoundingBox[1]", null);
		}
		catch (XPathExpressionException ex) { // ignore--expression ok
		}
		return Extents.coalesceBoundingBoxes(XMLUtils.getNodeListAsList(boxNodes));
	}

}
