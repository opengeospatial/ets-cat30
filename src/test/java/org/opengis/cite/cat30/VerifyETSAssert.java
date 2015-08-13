package org.opengis.cite.cat30;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class VerifyETSAssert {

    private static final String WADL_NS = "http://wadl.dev.java.net/2009/02";
    private static DocumentBuilder docBuilder;
    private static SchemaFactory factory;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VerifyETSAssert() {
    }

    @BeforeClass
    public static void setUpClass() throws ParserConfigurationException {
        factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void assertXPathWithNamespaceBindings() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities/basic.xml"));
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put(WADL_NS, "ns1");
        String xpath = "//ns1:resources";
        ETSAssert.assertXPath(xpath, doc, nsBindings);
    }

    @Test
    public void assertXPath_expectFalse() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Unexpected result evaluating XPath expression");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities/basic.xml"));
        // using built-in namespace bindings
        String xpath = "//ows:OperationsMetadata/ows:Constraint[@name='GetCapabilities-XML']/ows:DefaultValue = 'TRUE'";
        ETSAssert.assertXPath(xpath, doc, null);
    }

    @Test
    public void emptyAtomFeed() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom/feed-empty.xml"));
        ETSAssert.assertEmptyResultSet(doc);
    }

    @Test
    public void searchResponseNotEmpty() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("csw:SearchResults/@numberOfRecordsMatched = 0");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/GetRecordsResponse-1.xml"));
        ETSAssert.assertEmptyResultSet(doc);
    }

    @Test
    public void emptySearchResponse() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/GetRecordsResponse-empty.xml"));
        ETSAssert.assertEmptyResultSet(doc);
    }

    @Test
    public void searchTermOccursInAttribute()
            throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom/feed.xml"));
        NodeList records = doc.getElementsByTagNameNS(Namespaces.ATOM, "entry");
        ETSAssert.assertAllTermsOccur(records, "robotics");
    }

    @Test
    public void searchTermWithNonASCIICharDoesNotOccur()
            throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Unexpected result evaluating XPath expression");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom/feed.xml"));
        NodeList records = doc.getElementsByTagNameNS(Namespaces.ATOM, "entry");
        ETSAssert.assertAllTermsOccur(records, "données");
    }
}
