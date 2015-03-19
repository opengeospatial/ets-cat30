package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;

import org.junit.Test;
import org.opengis.cite.cat30.Namespaces;
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
    public void getOpenSearchQueryTemplates() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/opensearch/OpenSearchDescription-valid.xml"));
        List<Node> urlTemplates = ServiceMetadataUtils.getOpenSearchQueryTemplates(doc);
        assertEquals("Unexpected number of URL templates found.",
                2, urlTemplates.size());
        Node url1 = urlTemplates.get(0);
        Object userData = url1.getUserData(ServiceMetadataUtils.URL_TEMPLATE_PARAMS);
        assertNotNull("No user data.", userData);
        Map<QName, Boolean> templateParams = (Map<QName, Boolean>) userData;
        Boolean isRequired = templateParams.get(new QName("searchTerms"));
        assertTrue("searchTerms is required.", isRequired);
        isRequired = templateParams.get(new QName(Namespaces.OS_GEO, "box"));
        assertFalse("geo:box is optional.", isRequired);
    }

}
