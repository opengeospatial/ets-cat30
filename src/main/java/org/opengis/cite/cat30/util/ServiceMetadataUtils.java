package org.opengis.cite.cat30.util;

import java.net.URI;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat30.Namespaces;
import org.w3c.dom.Document;

/**
 * Provides various utility methods for accessing service-related metadata.
 */
public class ServiceMetadataUtils {

    /**
     * Extracts a request endpoint from a service capabilities document. If the
     * request URI contains a query component it is ignored.
     * 
     * @param cswMetadata
     *            A DOM Document node containing service metadata (OGC
     *            capabilities document).
     * @param opName
     *            The operation (request) name.
     * @param httpMethod
     *            The HTTP method to use (if {@code null} or empty the first
     *            supported method will be used).
     * @return A URI denoting a service endpoint; the URI is empty if no
     *         matching endpoint was found.
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
            // method name in OWS content model is "Get" | "Post"
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
        if (null != endpoint.getQuery()) {
            // prune query component if present
            String uri = endpoint.toString();
            endpoint = URI.create(uri.substring(0, uri.indexOf('?')));
        }
        return endpoint;
    }

}
