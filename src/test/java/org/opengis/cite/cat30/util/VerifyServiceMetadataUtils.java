package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;

import org.junit.Test;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.opensearch.TemplateParamInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the ServiceMetadataUtils class.
 */
public class VerifyServiceMetadataUtils {

    private static DocumentBuilder docBuilder;

    public VerifyServiceMetadataUtils() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void getOpenSearchURLTemplates() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/opensearch/OpenSearchDescription-valid.xml"));
        List<Node> urlTemplates = ServiceMetadataUtils.getOpenSearchURLTemplates(doc);
        assertEquals("Unexpected number of URL templates found.", 1, urlTemplates.size());
        Node url1 = urlTemplates.get(0);
        Object userData = url1.getUserData(ServiceMetadataUtils.URL_TEMPLATE_PARAMS);
        assertNotNull("No user data.", userData);
        List<TemplateParamInfo> templateParams = (List<TemplateParamInfo>) userData;
        TemplateParamInfo param1 = templateParams.get(0);
        assertEquals(param1.getName(), new QName(Namespaces.OSD11, "searchTerms"));
        assertTrue("searchTerms is required.", param1.isRequired());
        TemplateParamInfo param3 = templateParams.get(2);
        assertEquals(param3.getName(), new QName(Namespaces.OS_GEO, "box"));
    }

    @Test
    public void getOpenSearchDescriptionConstraint() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities/basic.xml"));
        Set<String> values = ServiceMetadataUtils.getConstraintValues(
                doc, "OpenSearchDescriptionDocument");
        assertEquals("Unexpected number of values.", 1, values.size());
        assertEquals(values.iterator().next(),
                "http://www.sdisuite.de/terraCatalog/opensearch");
    }

    @Test
    public void getAcceptVersionsParamValues() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities/basic.xml"));
        Set<String> versions = ServiceMetadataUtils.getParameterValues(
                doc, CAT3.GET_CAPABILITIES, CAT3.ACCEPT_VERSIONS);
        assertEquals("Unexpected number of supported versions.", 2, versions.size());
        assertTrue("Expected '3.0.0' as supported version.",
                versions.contains("3.0.0"));
    }

    @Test
    public void getOutputSchemaValuesForGetRecordById()
            throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities/basic.xml"));
        Set<String> schemas = ServiceMetadataUtils.getParameterValues(
                doc, CAT3.GET_RECORD_BY_ID, CAT3.OUTPUT_SCHEMA);
        assertEquals("Unexpected number of supported versions.", 2, schemas.size());
        assertTrue("Expected Atom as supported outputSchema.",
                schemas.contains(Namespaces.ATOM));
    }
}
