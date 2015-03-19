package org.opengis.cite.cat30.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat30.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides various utility methods for accessing service-related metadata
 * resources such as an OGC service description or an OpenSearch description
 * document.
 */
public class ServiceMetadataUtils {

    /**
     * Key value that associates an Element node (osd:Url in an OpenSearch
     * description document) with the collection of URL template parameters it
     * declares.
     */
    public static final String URL_TEMPLATE_PARAMS = "url.template.params";

    /**
     * Extracts a request endpoint from a service capabilities document. If the
     * request URI contains a query component it is ignored.
     *
     * @param cswMetadata A DOM Document node containing service metadata (OGC
     * capabilities document).
     * @param opName The operation (request) name.
     * @param httpMethod The HTTP method to use (if {@code null} or empty the
     * first method listed will be used).
     *
     * @return A URI denoting a service endpoint; the URI is empty if no
     * matching endpoint was found.
     */
    public static URI getOperationEndpoint(final Document cswMetadata,
            String opName, String httpMethod) {
        String expr;
        if (null == httpMethod || httpMethod.isEmpty()) {
            // use first supported method
            expr = String.format(
                    "//ows:Operation[@name='%s']//ows:HTTP/*[1]/@xlink:href",
                    opName);
        } else {
            // method name in OWS content model is "Get" or "Post"
            StringBuilder method = new StringBuilder(httpMethod);
            method.replace(1, method.length(), method.substring(1)
                    .toLowerCase());
            expr = String.format(
                    "//ows:Operation[@name='%s']//ows:%s/@xlink:href", opName,
                    method.toString());
        }
        NamespaceBindings nsBindings = new NamespaceBindings();
        nsBindings.addNamespaceBinding(Namespaces.OWS, "ows");
        nsBindings.addNamespaceBinding(Namespaces.XLINK, "xlink");
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsBindings);
        URI endpoint = null;
        try {
            String href = xpath.evaluate(expr, cswMetadata);
            endpoint = URI.create(href);
        } catch (XPathExpressionException ex) {
            // XPath expression is correct
            TestSuiteLogger.log(Level.INFO, ex.getMessage());
        }
        if (null != endpoint && null != endpoint.getQuery()) {
            // prune query component if present
            String uriRef = endpoint.toString();
            endpoint = URI.create(uriRef.substring(0, uriRef.indexOf('?')));
        }
        return endpoint;
    }

    /**
     * Returns a list of nodes representing the query templates defined in an
     * OpenSearch description document. Each node in the list will be associated
     * with a {@code Map<QName,Boolean>} containing the declared template
     * parameters; it can be accessed via
     * {@link Node#getUserData(java.lang.String) getUserData} using the key
     * value {@link #URL_TEMPLATE_PARAMS}.
     * <p>
     * For each map entry the key is a QName specifying the parameter name; a
     * Boolean value indicates whether or not the query parameter is required.
     * </p>
     *
     * @param osDescr An OpenSearchDescription document
     * (osd:OpenSearchDescription).
     * @return A sequence of Element nodes (osd:Url) containing URL templates.
     */
    public static List<Node> getOpenSearchQueryTemplates(final Document osDescr) {
        List<Node> urlList = new ArrayList<>();
        NodeList nodes = osDescr.getElementsByTagNameNS(Namespaces.OSD11, "Url");
        Pattern queryParam = Pattern.compile("\\{([^}]+)\\}");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element url = (Element) nodes.item(i);
            String template = url.getAttribute("template");
            Matcher matcher = queryParam.matcher(template);
            Map<QName, Boolean> templateParams = new HashMap<>();
            while (matcher.find()) {
                String param = matcher.group(1); // first capturing group
                Boolean isRequired = !param.endsWith("?");
                String[] qName = param.split(":");
                if (qName.length > 1) {
                    String nsPrefix = qName[0];
                    String nsURI = url.lookupNamespaceURI(nsPrefix);
                    templateParams.put(new QName(nsURI,
                            qName[1].replace("?", ""), nsPrefix), isRequired);
                } else {
                    templateParams.put(new QName(qName[0].replace("?", "")),
                            isRequired);
                }
            }
            url.setUserData(URL_TEMPLATE_PARAMS, templateParams, null);
            urlList.add(url);
        }
        return urlList;
    }

}
