package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;

import org.junit.Test;
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
                "/capabilities-basic.xml"));
        List<String> values = ServiceMetadataUtils.getConstraintValues(
                doc, "OpenSearchDescriptionDocument");
        assertEquals("Unexpected number of values.", 1, values.size());
        assertEquals(values.get(0), "http://www.sdisuite.de/terraCatalog/opensearch");
    }
}
