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
import org.xml.sax.SAXException;

public class VerifyETSAssert {

    private static DocumentBuilder docBuilder;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public VerifyETSAssert() {
    }

    @BeforeClass
    public static void setUpClass() throws ParserConfigurationException {
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void assertXPathWithNamespaceBindings() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities-basic.xml"));
        Map<String, String> nsBindings = new HashMap<String, String>();
        nsBindings.put(Namespaces.FES, "fes");
        String xpath = "//fes:Conformance";
        ETSAssert.assertXPath(xpath, doc, nsBindings);
    }

    @Test
    public void assertXPath_expectFalse() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Unexpected result evaluating XPath expression");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities-basic.xml"));
        // using built-in namespace binding
        String xpath = "//ows:OperationsMetadata/ows:Constraint[@name='Undefined']/ows:DefaultValue = 'TRUE'";
        ETSAssert.assertXPath(xpath, doc, null);
    }
}
