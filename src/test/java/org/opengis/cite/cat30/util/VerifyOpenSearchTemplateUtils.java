package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import org.opengis.cite.cat30.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the ServiceMetadataUtils class.
 */
public class VerifyOpenSearchTemplateUtils {

	private static DocumentBuilder docBuilder;

	public VerifyOpenSearchTemplateUtils() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void buildRequestURIWithBox() throws SAXException, IOException {
		Document doc = docBuilder
			.parse(this.getClass().getResourceAsStream("/opensearch/OpenSearchDescription-valid.xml"));
		List<Node> urlTemplates = ServiceMetadataUtils.getOpenSearchURLTemplates(doc);
		Element url1 = (Element) urlTemplates.get(0);
		Map<QName, String> values = new HashMap<>();
		values.put(new QName(Namespaces.OSD11, "searchTerms"), "alpha");
		values.put(new QName(Namespaces.OS_GEO, "box"), "-123.45,48.99,-122.45,49.49");
		URI uri = OpenSearchTemplateUtils.buildRequestURI(url1, values);
		String query = uri.getQuery();
		assertEquals("q=alpha&pw=1&box=-123.45,48.99,-122.45,49.49&format=atom", query);
	}

	@Test
	public void buildRequestURIWithIllegalChars() throws SAXException, IOException {
		Document doc = docBuilder
			.parse(this.getClass().getResourceAsStream("/opensearch/OpenSearchDescription-id.xml"));
		List<Node> urlTemplates = ServiceMetadataUtils.getOpenSearchURLTemplates(doc);
		Element url1 = (Element) urlTemplates.get(0);
		Map<QName, String> values = new HashMap<>();
		String id = "{5d0060fe-d5c4-4307-acc4-e0810c21b6aa}";
		values.put(new QName(Namespaces.OS_GEO, "uid"), URIUtils.getPercentEncodedString(id));
		URI uri = OpenSearchTemplateUtils.buildRequestURI(url1, values);
		assertEquals("q=&pw=1&id={5d0060fe-d5c4-4307-acc4-e0810c21b6aa}", uri.getQuery());
	}

}
