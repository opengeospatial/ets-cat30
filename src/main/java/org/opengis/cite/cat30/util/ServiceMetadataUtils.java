package org.opengis.cite.cat30.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.opensearch.TemplateParamInfo;
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
     * Returns a list of nodes representing the URL templates defined in an
     * OpenSearch description document. Each node in the resulting list will be
     * associated with an unmodifiable {@code List<TemplateParamInfo>}
     * containing information about the declared template parameters; it can be
     * accessed via {@link Node#getUserData(java.lang.String) getUserData} using
     * the key value {@link #URL_TEMPLATE_PARAMS}.
     *
     * @param osDescr An OpenSearchDescription document
     * (osd:OpenSearchDescription).
     * @return A sequence of Element nodes (os:Url) containing URL templates.
     */
    public static List<Node> getOpenSearchURLTemplates(final Document osDescr) {
        List<Node> urlList = new ArrayList<>();
        NodeList nodes = osDescr.getElementsByTagNameNS(Namespaces.OSD11, "Url");
        Pattern queryParam = Pattern.compile("\\{([^}]+)\\}");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element urlElem = (Element) nodes.item(i);
            String template = urlElem.getAttribute("template");
            Matcher matcher = queryParam.matcher(template);
            List<TemplateParamInfo> templateParamList = new ArrayList<>();
            while (matcher.find()) {
                TemplateParamInfo paramInfo = new TemplateParamInfo();
                String param = matcher.group(1); // first capturing group
                paramInfo.setIsRequired(!param.endsWith("?"));
                QName paramQName = OpenSearchTemplateUtils.getTemplateParameterName(param, urlElem);
                paramInfo.setName(paramQName);
                templateParamList.add(paramInfo);
                if (paramQName.getNamespaceURI().equals(Namespaces.OSD11)) {
                    OpenSearchTemplateUtils.updateOpenSearchParameter(paramInfo, urlElem);
                }
            }
            urlElem.setUserData(URL_TEMPLATE_PARAMS,
                    Collections.unmodifiableList(templateParamList), null);
            urlList.add(urlElem);
        }
        return urlList;
    }

    /**
     * Returns a list of nodes representing queries defined in an OpenSearch
     * description document.
     *
     * @param osDescr An OpenSearchDescription document.
     * @param role The (qualified) name of a query role.
     * @return A sequence of Element nodes (os:Query) that define specific
     * search requests; the list may be empty if no matching queries are found.
     */
    public static List<Node> getOpenSearchQueriesByRole(final Document osDescr,
            QName role) {
        List<Node> queryList = new ArrayList<>();
        NodeList nodes = osDescr.getElementsByTagNameNS(Namespaces.OSD11, "Query");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element query = (Element) nodes.item(i);
            QName roleName = OpenSearchTemplateUtils.getTemplateParameterName(
                    query.getAttribute("role"), query);
            if (roleName.equals(role)) {
                queryList.add(query);
            }
        }
        return queryList;
    }

    /**
     * Searches a CSW capabilities document for the specified constraint and
     * returns its allowed values. If there is more than one constraint with the
     * same name in the document, all but the first are ignored. The default
     * value is returned if present; otherwise the complete list of allowed
     * values.
     *
     * @param cswMetadata A CSW capabilities document.
     * @param name The name of the constraint.
     * @return A list containing the allowed values of the constraint, or null
     * if no such constraint exists or it has no value.
     */
    public static List<String> getConstraintValues(final Document cswMetadata,
            String name) {
        String xpath = String.format("//ows:Constraint[@name='%s']", name);
        NodeList nodeList = null;
        try {
            nodeList = XMLUtils.evaluateXPath(cswMetadata, xpath, null);
        } catch (XPathExpressionException ex) {
            // expression is ok
        }
        List<String> valueList = null;
        if (null != nodeList && nodeList.getLength() > 0) {
            Element constraint = (Element) nodeList.item(0);
            nodeList = constraint.getElementsByTagNameNS(Namespaces.OWS, "DefaultValue");
            if (nodeList.getLength() == 0) {
                nodeList = constraint.getElementsByTagNameNS(Namespaces.OWS, "Value");
            }
            if (nodeList.getLength() > 0) {
                valueList = new ArrayList<String>();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    valueList.add(nodeList.item(i).getTextContent());
                }
            }
        }
        return valueList;
    }

}
