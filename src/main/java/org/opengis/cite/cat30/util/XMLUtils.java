package org.opengis.cite.cat30.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.ws.rs.core.MediaType;
import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Provides various utility methods for accessing or manipulating XML representations.
 */
public class XMLUtils {

	private static final Logger LOGR = Logger.getLogger(XMLUtils.class.getPackage().getName());

	private static final XMLInputFactory STAX_FACTORY = initXMLInputFactory();

	private static final XPathFactory XPATH_FACTORY = initXPathFactory();

	private static XPathFactory initXPathFactory() {
		XPathFactory factory = XPathFactory.newInstance();
		return factory;
	}

	private static XMLInputFactory initXMLInputFactory() {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		return factory;
	}

	/**
	 * Writes the content of a DOM Node to a string. The XML declaration is omitted and
	 * the character encoding is set to "US-ASCII" (any character outside of this set is
	 * serialized as a numeric character reference).
	 * @param node The DOM Node to be serialized.
	 * @return A String representing the content of the given node.
	 */
	public static String writeNodeToString(Node node) {
		if (null == node) {
			return "";
		}
		Writer writer = null;
		try {
			Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.ENCODING, "US-ASCII");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			idTransformer.setOutputProperties(outProps);
			writer = new StringWriter();
			idTransformer.transform(new DOMSource(node), new StreamResult(writer));
		}
		catch (TransformerException ex) {
			TestSuiteLogger.log(Level.WARNING, "Failed to serialize node " + node.getNodeName(), ex);
		}
		return writer.toString();
	}

	/**
	 * Writes the content of a DOM Node to a byte stream. An XML declaration is always
	 * omitted.
	 * @param node The DOM Node to be serialized.
	 * @param outputStream The destination OutputStream reference.
	 */
	public static void writeNode(Node node, OutputStream outputStream) {
		try {
			Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.METHOD, "xml");
			outProps.setProperty(OutputKeys.ENCODING, "UTF-8");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			idTransformer.setOutputProperties(outProps);
			idTransformer.transform(new DOMSource(node), new StreamResult(outputStream));
		}
		catch (TransformerException ex) {
			String nodeName = (node.getNodeType() == Node.DOCUMENT_NODE)
					? Document.class.cast(node).getDocumentElement().getNodeName() : node.getNodeName();
			TestSuiteLogger.log(Level.WARNING, "Failed to serialize DOM node: " + nodeName, ex);
		}
	}

	/**
	 * Evaluates an XPath 1.0 expression using the given context and returns the result as
	 * a node set.
	 * @param context The context node.
	 * @param expr An XPath expression.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value).
	 * Standard bindings do not need to be declared (see
	 * {@link org.opengis.cite.cat30.util.NamespaceBindings#withStandardBindings()}.
	 * @return A NodeList containing nodes that satisfy the expression (it may be empty).
	 * @throws javax.xml.xpath.XPathExpressionException If the expression cannot be
	 * evaluated for any reason.
	 */
	public static NodeList evaluateXPath(Node context, String expr, Map<String, String> namespaceBindings)
			throws XPathExpressionException {
		Object result = evaluateXPath(new DOMSource(context), expr, namespaceBindings, XPathConstants.NODESET);
		if (!NodeList.class.isInstance(result)) {
			throw new XPathExpressionException("Expression does not evaluate to a NodeList: " + expr);
		}
		return (NodeList) result;
	}

	/**
	 * Evaluates an XPath expression using the given context item and returns the result
	 * as the specified type.
	 *
	 * <p>
	 * <strong>Note:</strong> The Saxon implementation supports XPath 2.0 expressions when
	 * using the JAXP XPath APIs (the default implementation will throw an exception).
	 * </p>
	 * @param context The context item.
	 * @param expr An XPath expression.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value).
	 * Standard bindings do not need to be declared (see
	 * {@link org.opengis.cite.cat30.util.NamespaceBindings#withStandardBindings()}.
	 * @param returnType The desired return type (as declared in
	 * {@link javax.xml.xpath.XPathConstants} ).
	 * @return The result converted to the desired returnType.
	 * @throws javax.xml.xpath.XPathExpressionException If the expression cannot be
	 * evaluated for any reason.
	 */
	public static Object evaluateXPath(Source context, String expr, Map<String, String> namespaceBindings,
			QName returnType) throws XPathExpressionException {
		Node contextNode = null;
		if (DOMSource.class.isInstance(context)) {
			contextNode = DOMSource.class.cast(context).getNode();
		}
		else {
			try {
				contextNode = parse(context);
			}
			catch (TransformerException ex) {
				TestSuiteLogger.log(Level.WARNING, "Failed to read context item. ", ex);
			}
		}
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPathFactory factory = XPATH_FACTORY;
		// WARNING: If context node is Saxon NodeOverNodeInfo, the factory must
		// use the same Configuration object to avoid IllegalArgumentException
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(bindings);
		Object result = xpath.evaluate(expr, contextNode, returnType);
		return result;
	}

	/**
	 * Evaluates an XPath 2.0 expression using the Saxon s9api interfaces.
	 * @param xmlSource The XML Source.
	 * @param expr The XPath expression to be evaluated.
	 * @param nsBindings A collection of namespace bindings required to evaluate the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value); this
	 * may be {@code null} if not needed.
	 * @return An XdmValue object representing a value in the XDM data model; this is a
	 * sequence of zero or more items, where each item is either an atomic value or a
	 * node.
	 * @throws net.sf.saxon.s9api.SaxonApiException If an error occurs while evaluating
	 * the expression; this always wraps some other underlying exception.
	 */
	public static XdmValue evaluateXPath2(Source xmlSource, String expr, Map<String, String> nsBindings)
			throws SaxonApiException {
		Processor proc = new Processor(false);
		XPathCompiler compiler = proc.newXPathCompiler();
		if (null != nsBindings) {
			for (String nsURI : nsBindings.keySet()) {
				compiler.declareNamespace(nsBindings.get(nsURI), nsURI);
			}
		}
		XPathSelector xpath = compiler.compile(expr).load();
		DocumentBuilder builder = proc.newDocumentBuilder();
		XdmNode node = null;
		if (DOMSource.class.isInstance(xmlSource)) {
			DOMSource domSource = (DOMSource) xmlSource;
			node = builder.wrap(domSource.getNode());
		}
		else {
			node = builder.build(xmlSource);
		}
		xpath.setContextItem(node);
		return xpath.evaluate();
	}

	/**
	 * Evaluates an XQuery 1.0 expression using the Saxon s9api interfaces.
	 * @param source The XML Source.
	 * @param query The query expression.
	 * @param nsBindings A collection of namespace bindings required to evaluate the
	 * query, where each entry maps a namespace URI (key) to a prefix (value).
	 * @return An XdmValue object representing a value in the XDM data model.
	 * @throws net.sf.saxon.s9api.SaxonApiException If an error occurs while evaluating
	 * the query (this always wraps some other underlying exception).
	 */
	public static XdmValue evaluateXQuery(Source source, String query, Map<String, String> nsBindings)
			throws SaxonApiException {
		Processor proc = new Processor(false);
		XQueryCompiler xqCompiler = proc.newXQueryCompiler();
		if (null != nsBindings) {
			for (String nsURI : nsBindings.keySet()) {
				xqCompiler.declareNamespace(nsBindings.get(nsURI), nsURI);
			}
		}
		XQueryExecutable xqExec = xqCompiler.compile(query);
		XQueryEvaluator xqEval = xqExec.load();
		xqEval.setSource(source);
		return xqEval.evaluate();
	}

	/**
	 * Creates a new Element having the specified qualified name. The element must be
	 * {@link org.w3c.dom.Document#adoptNode(Node) adopted} when inserted into another
	 * Document.
	 * @param qName A QName object.
	 * @return An Element node (with a Document owner but no parent).
	 */
	public static Element createElement(QName qName) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Element elem = doc.createElementNS(qName.getNamespaceURI(), qName.getLocalPart());
		return elem;
	}

	/**
	 * Returns a List of all descendant Element nodes having the specified [namespace
	 * name] property. The elements are listed in document order.
	 * @param node The node to search from.
	 * @param namespaceURI An absolute URI denoting a namespace name.
	 * @return A List containing elements in the specified namespace; the list is empty if
	 * there are no elements in the namespace.
	 */
	public static List<Element> getElementsByNamespaceURI(Node node, String namespaceURI) {
		List<Element> list = new ArrayList<Element>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (child.getNamespaceURI().equals(namespaceURI)) {
				list.add((Element) child);
			}
		}
		return list;
	}

	/**
	 * Transforms the content of a DOM Node using a specified XSLT stylesheet.
	 * @param xslt A Source object representing a stylesheet (XSLT 1.0 or 2.0).
	 * @param source A Node representing the XML source. If it is an Element node it will
	 * be imported into a new DOM Document.
	 * @return A DOM Document containing the result of the transformation.
	 */
	public static Document transform(Source xslt, Node source) {
		Document sourceDoc = null;
		Document resultDoc = null;
		try {
			resultDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			if (source.getNodeType() == Node.DOCUMENT_NODE) {
				sourceDoc = (Document) source;
			}
			else {
				sourceDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				sourceDoc.appendChild(sourceDoc.importNode(source, true));
			}
		}
		catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		}
		Processor processor = new Processor(false);
		XsltCompiler compiler = processor.newXsltCompiler();
		try {
			XsltExecutable exec = compiler.compile(xslt);
			XsltTransformer transformer = exec.load();
			transformer.setSource(new DOMSource(sourceDoc));
			transformer.setDestination(new DOMDestination(resultDoc));
			transformer.transform();
		}
		catch (SaxonApiException e) {
			throw new RuntimeException(e);
		}
		return resultDoc;
	}

	/**
	 * Expands character entity ({@literal  &name;}) and numeric references
	 * ({@literal &#xhhhh;} or {@literal &dddd;}) that occur within a given string value.
	 * It may be necessary to do this before processing an XPath expression.
	 * @param value A string representing text content.
	 * @return A string with all included references expanded.
	 */
	public static String expandReferencesInText(String value) {
		StringBuilder wrapper = new StringBuilder("<value>");
		wrapper.append(value).append("</value>");
		Reader reader = new StringReader(wrapper.toString());
		String str = null;
		try {
			XMLStreamReader xsr = STAX_FACTORY.createXMLStreamReader(reader);
			xsr.nextTag(); // document element
			str = xsr.getElementText();
		}
		catch (XMLStreamException xse) {
			LOGR.log(Level.WARNING, xse.getMessage(), xse);
		}
		return str;
	}

	/**
	 * Converts a DOMSource object to a StreamSource representing an XML data source. The
	 * system ID is preserved, allowing relative URIs to be processed.
	 * @param domSource A DOMSource instance.
	 * @return A StreamSource object for reading the content represented by the original
	 * DOM tree.
	 */
	public static StreamSource toStreamSource(DOMSource domSource) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(baos);
		try {
			// use identity transformer
			Transformer idt = TransformerFactory.newInstance().newTransformer();
			idt.transform(domSource, result);
		}
		catch (TransformerException tex) {
			LOGR.log(Level.WARNING, "Error serializing DOMSource " + domSource.getSystemId(), tex);
		}
		StreamSource streamSrc = new StreamSource(new ByteArrayInputStream(baos.toByteArray()),
				domSource.getSystemId());
		return streamSrc;
	}

	/**
	 * Returns the name of the document element in the given XML resource.
	 * @param source The Source to read the document from.
	 * @return The qualified name of the document element, or <code>null</code> if the
	 * source is not an XML document or it cannot be read for some reason.
	 */
	public static QName nameOfDocumentElement(Source source) {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		QName qName = null;
		try {
			XMLEventReader reader = factory.createXMLEventReader(source);
			// advance to document element
			StartElement docElem = reader.nextTag().asStartElement();
			qName = docElem.getName();
		}
		catch (XMLStreamException xse) {
			LOGR.log(Level.WARNING, "Failed to read Source.", xse);
		}
		return qName;
	}

	/**
	 * Parses the content of the given Source and returns a DOM Document node.
	 * @param source The Source to read the XML content from.
	 * @return A Document node representing the XML content.
	 * @throws javax.xml.transform.TransformerException If the source cannot be parsed for
	 * any reason.
	 */
	public static Document parse(Source source) throws TransformerException {
		Transformer idt = TransformerFactory.newInstance().newTransformer();
		DOMResult result = new DOMResult();
		idt.transform(source, result);
		Document doc = (Document) result.getNode();
		if (null != doc) {
			doc.setDocumentURI(source.getSystemId());
		}
		return doc;
	}

	/**
	 * Returns a List view of the specified NodeList collection.
	 * @param nodeList An ordered collection of DOM nodes.
	 * @return A List containing the original sequence of Node objects.
	 */
	public static List<Node> getNodeListAsList(NodeList nodeList) {
		List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			nodes.add(nodeList.item(i));
		}
		return nodes;
	}

	/**
	 * Writes the content of an XdmValue sequence to a string. Each item in the sequence
	 * is either an atomic value or a node.
	 * @param value A value in the XDM data model.
	 * @return A String representing the content of the sequence.
	 * @see <a target="_blank" href=
	 * "http://www.saxonica.com/html/documentation/javadoc/net/sf/saxon/s9api/XdmValue.html">Saxon
	 * API: XdmValue</a>
	 * @see <a target="_blank" href="http://www.w3.org/TR/xpath-datamodel/">XQuery 1.0 and
	 * XPath 2.0 Data Model (XDM) (Second Edition)</a>
	 */
	public static String writeXdmValueToString(XdmValue value) {
		StringBuilder str = new StringBuilder();
		for (XdmItem item : value) {
			if (item.isAtomicValue()) {
				str.append(item.getStringValue());
			}
			else {
				XdmNode node = (XdmNode) item;
				str.append(node.getNodeName()).append(": ");
				str.append(node.getStringValue());
			}
			str.append('\n');
		}
		return str.toString();
	}

	/**
	 * Determines if the given media type is an XML-based media type.
	 * @param mediaType A MediaType object.
	 * @return true if the type corresponds to an XML entity; false otherwise.
	 * @see <a href="https://tools.ietf.org/html/rfc7303" target="_blank">RFC 7303: XML
	 * Media Types</a>
	 */
	public static boolean isXML(final MediaType mediaType) {
		return mediaType.getSubtype().endsWith("xml");
	}

	/**
	 * Returns the text content of the nodes in the given list.
	 * @param nodeList A sequence of DOM nodes.
	 * @return A list of String values, each of which represents the content of a node
	 * (and its descendants, if any).
	 */
	public static List<String> getNodeValues(NodeList nodeList) {
		List<String> valueList = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			valueList.add(nodeList.item(i).getTextContent());
		}
		return valueList;
	}

}
