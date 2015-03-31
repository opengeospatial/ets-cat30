package org.opengis.cite.cat30.basic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.net.URI;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.opengis.cite.cat30.util.XMLUtils;
import org.opengis.cite.geomatics.Extents;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VerifyGetRecordsKVPTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    private static Schema cswSchema;
    private static Schema atomSchema;

    @BeforeClass
    public static void initTestFixture() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        Document doc = docBuilder.parse(VerifyGetRecordsKVPTests.class.getResourceAsStream(
                "/capabilities-basic.xml"));
        when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
        cswSchema = ValidationUtils.createCSWSchema();
        when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName()))
                .thenReturn(cswSchema);
        atomSchema = ValidationUtils.createAtomSchema();
        when(suite.getAttribute(SuiteAttribute.ATOM_SCHEMA.getName()))
                .thenReturn(atomSchema);
    }

    @Test
    public void getBriefRecordsByBBOX_allIntersect()
            throws SAXException, IOException, FactoryException, TransformException {
        Client client = mock(Client.class);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        ClientResponse rsp = mock(ClientResponse.class);
        when(client.handle(any(ClientRequest.class))).thenReturn(rsp);
        when(rsp.getStatus()).thenReturn(
                ClientResponse.Status.OK.getStatusCode());
        Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/GetRecordsResponse-full.xml"));
        when(rsp.getEntity(Document.class)).thenReturn(rspEntity);
        GetRecordsKVPTests iut = new GetRecordsKVPTests();
        iut.initCommonFixture(testContext);
        // BOX2D(32.5 -117.6, 34 -115) with CRS EPSG:4326
        iut.setExtent(buildEnvelope(1));
        iut.setGetEndpoint(URI.create("http://localhost/csw/v3"));
        iut.getBriefRecordsByBBOX();
    }

    @Test
    public void getBriefRecordsByBBOX_noneIntersect()
            throws SAXException, IOException, FactoryException, TransformException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("The envelopes do not intersect");
        Client client = mock(Client.class);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        ClientResponse rsp = mock(ClientResponse.class);
        when(client.handle(any(ClientRequest.class))).thenReturn(rsp);
        when(rsp.getStatus()).thenReturn(
                ClientResponse.Status.OK.getStatusCode());
        Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/GetRecordsResponse-full.xml"));
        when(rsp.getEntity(Document.class)).thenReturn(rspEntity);
        GetRecordsKVPTests iut = new GetRecordsKVPTests();
        iut.initCommonFixture(testContext);
        // BOX2D(472944 5363287, 516011 5456383) with CRS EPSG:32610
        iut.setExtent(buildEnvelope(2));
        iut.setGetEndpoint(URI.create("http://localhost/csw/v3"));
        iut.getBriefRecordsByBBOX();
    }

    @Test
    public void getSummaryRecordsByWGS84BBOX_noneIntersect()
            throws SAXException, IOException, FactoryException, TransformException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("The envelopes do not intersect");
        Client client = mock(Client.class);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        ClientResponse rsp = mock(ClientResponse.class);
        when(client.handle(any(ClientRequest.class))).thenReturn(rsp);
        when(rsp.getStatus()).thenReturn(
                ClientResponse.Status.OK.getStatusCode());
        Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/GetRecordsResponse-full.xml"));
        when(rsp.getEntity(Document.class)).thenReturn(rspEntity);
        GetRecordsKVPTests iut = new GetRecordsKVPTests();
        iut.initCommonFixture(testContext);
        // BOX2D(472944 5363287, 516011 5456383) with CRS EPSG:32610
        iut.setExtent(buildEnvelope(2));
        iut.setGetEndpoint(URI.create("http://localhost/csw/v3"));
        iut.getSummaryRecordsByWGS84BBOX();
    }

    private Envelope buildEnvelope(int id)
            throws SAXException, IOException, FactoryException, TransformException {
        String path = String.format("/rsp/GetRecordsResponse-%d.xml", id);
        Document doc = docBuilder.parse(getClass().getResourceAsStream(path));
        NodeList boxNodes = null;
        try {
            boxNodes = XMLUtils.evaluateXPath(doc,
                    "//csw:Record/ows:BoundingBox[1] | //csw:Record/ows:WGS84BoundingBox[1]", null);
        } catch (XPathExpressionException ex) { // ignore--expression ok
        }
        return Extents.coalesceBoundingBoxes(XMLUtils.getNodeListAsList(boxNodes));
    }

}
