package org.opengis.cite.cat30.util;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.opensearch.TemplateParamInfo;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Provides various utility methods for processing OpenSearch URL templates.
 */
public class OpenSearchTemplateUtils {

	/**
	 * Returns the qualified name of the given OpenSearch URL template parameter.
	 * @param param A parameter in a URL template (e.g. "searchTerms", "geo:box?").
	 * @param contextNode The context node; this is used to perform a namespace lookup if
	 * necessary.
	 * @return A QName representing the name of the template parameter.
	 */
	public static QName getTemplateParameterName(String param, Node contextNode) {
		QName paramName;
		String[] qName = param.split(":");
		if (qName.length > 1) {
			String nsPrefix = qName[0];
			String nsURI = contextNode.lookupNamespaceURI(nsPrefix);
			paramName = new QName(nsURI, qName[1].replace("?", ""), nsPrefix);
		}
		else {
			paramName = new QName(Namespaces.OSD11, qName[0].replace("?", ""));
		}
		return paramName;
	}

	/**
	 * Filters a list of OpenSearch URL templates to include only those that contain the
	 * specified parameter.
	 * @param urlTemplates A list of (Element) nodes representing URL templates.
	 * @param paramName The qualified name of a template parameter.
	 * @return A new list containing templates with the specified parameter.
	 */
	public static List<Node> filterURLTemplatesByParam(List<Node> urlTemplates, QName paramName) {
		List<Node> templates = new ArrayList<>();
		for (Node urlTemplate : urlTemplates) {
			List<TemplateParamInfo> paramList = (List<TemplateParamInfo>) urlTemplate
				.getUserData(ServiceMetadataUtils.URL_TEMPLATE_PARAMS);
			if (null != paramList && !paramList.isEmpty()) {
				for (TemplateParamInfo paramInfo : paramList) {
					if (paramInfo.getName().equals(paramName)) {
						templates.add(urlTemplate);
						break;
					}
				}
			}
		}
		return templates;
	}

	/**
	 * Builds a request URI by processing a URL template according to the given parameter
	 * substitution values. The default value is used if there is no replacement value for
	 * a parameter.
	 * @param urlElem An Element node that represents a query endpoint (osd:Url).
	 * @param values A Map containing replacement values for template parameters.
	 * @return A URI representing the resulting request URI.
	 */
	public static URI buildRequestURI(Element urlElem, Map<QName, String> values) {
		List<TemplateParamInfo> paramList = (List<TemplateParamInfo>) urlElem
			.getUserData(ServiceMetadataUtils.URL_TEMPLATE_PARAMS);
		Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
		String template = urlElem.getAttribute("template");
		Matcher matcher = paramPattern.matcher(template);
		while (matcher.find()) {
			String param = matcher.group(0); // entire expression with braces
			QName qName = getTemplateParameterName(matcher.group(1), urlElem);
			if (values.containsKey(qName)) {
				template = template.replace(param, values.get(qName));
			}
			else {
				template = template.replace(param, getDefaultParamValue(paramList, qName));
			}
		}
		return URI.create(template);
	}

	/**
	 * Returns the default value of the specified template parameter.
	 * @param paramList A list of TemplateParamInfo objects describing the declared
	 * template parameters.
	 * @param paramName The qualified name of a template parameter.
	 * @return A String representing the default value (possibly an empty string).
	 */
	public static String getDefaultParamValue(List<TemplateParamInfo> paramList, QName paramName) {
		String value = null;
		for (TemplateParamInfo paramInfo : paramList) {
			if (paramInfo.getName().equals(paramName)) {
				value = paramInfo.getDefaultValue().toString();
				break;
			}
		}
		return value;
	}

	/**
	 * Updates the type and default value of a standard OpenSearch parameter.
	 * @param paramInfo A TemplateParamInfo object describing a standard OpenSearch
	 * parameter.
	 * @param urlElem An Element node (osd:Url) that defines an OpenSearch request
	 * containing the parameter.
	 *
	 * @see <a target="_blank" href=
	 * "http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_1.1_parameters">OpenSearch
	 * 1.1 parameters</a>
	 */
	public static void updateOpenSearchParameter(TemplateParamInfo paramInfo, Element urlElem) {
		String paramName = paramInfo.getName().getLocalPart();
		switch (paramName) {
			case "count":
				paramInfo.setType(Integer.class);
				break;
			case "startIndex":
				paramInfo.setType(Integer.class);
				String indexOffset = urlElem.getAttribute("indexOffset");
				Integer defaultValue = (indexOffset.isEmpty()) ? 1 : Integer.decode(indexOffset);
				paramInfo.setDefaultValue(defaultValue);
				break;
			case "startPage":
				paramInfo.setType(Integer.class);
				String pageOffset = urlElem.getAttribute("pageOffset");
				defaultValue = (pageOffset.isEmpty()) ? 1 : Integer.decode(pageOffset);
				paramInfo.setDefaultValue(defaultValue);
				break;
			case "language":
				paramInfo.setType(String.class);
				paramInfo.setDefaultValue("*");
				break;
			case "inputEncoding":
			case "outputEncoding":
				paramInfo.setType(Charset.class);
				paramInfo.setDefaultValue(StandardCharsets.UTF_8);
				break;
			default:
				paramInfo.setType(String.class);
		}
	}

	/**
	 * Extracts the actual parameters from an OpenSearch query specification.
	 * @param query A Node representing an osd:Query element.
	 * @return A Map containing the query parameters, where the key is the (qualified)
	 * parameter name.
	 */
	public static Map<QName, String> getQueryParameters(Node query) {
		Map<QName, String> params = new HashMap<>();
		NamedNodeMap attribs = query.getAttributes();
		List<String> metadataAttribs = Arrays.asList(new String[] { "role", "title", "totalResults" });
		for (int i = 0; i < attribs.getLength(); i++) {
			Node attr = attribs.item(i);
			String attrName = attr.getNodeName();
			if (metadataAttribs.contains(attrName)) {
				continue; // ignore metadata attributes
			}
			QName paramName = getTemplateParameterName(attrName, query);
			params.put(paramName, attr.getNodeValue());
		}
		return params;
	}

}
