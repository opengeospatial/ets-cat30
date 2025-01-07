package org.opengis.cite.cat30.util;

import com.thaiopensource.validation.Constants;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xerces.util.XMLCatalogResolver;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.XmlSchemaCompiler;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 * A utility class that provides convenience methods to support schema validation.
 */
public class ValidationUtils {

	/**
	 * ROOT_PKG
	 */
	static final String ROOT_PKG = "/org/opengis/cite/cat30/";

	/**
	 * FACTORY_RELAXNG_COMPACT
	 */
	static final String FACTORY_RELAXNG_COMPACT = "com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory";

	private static final XMLCatalogResolver SCH_RESOLVER = initCatalogResolver();

	private static XMLCatalogResolver initCatalogResolver() {
		return (XMLCatalogResolver) createSchemaResolver(Namespaces.SCH);
	}

	/**
	 * Creates a resource resolver suitable for locating schemas using an entity catalog.
	 * In effect, local copies of standard schemas are returned instead of retrieving them
	 * from external repositories.
	 * @param schemaLanguage A URI that identifies a schema language by namespace name.
	 * @return A {@code LSResourceResolver} object that is configured to use an OASIS
	 * entity catalog.
	 */
	public static LSResourceResolver createSchemaResolver(URI schemaLanguage) {
		String catalogFileName;
		if (schemaLanguage.equals(Namespaces.XSD)) {
			catalogFileName = "schema-catalog.xml";
		}
		else {
			catalogFileName = "schematron-catalog.xml";
		}
		URL catalogURL = ValidationUtils.class.getResource(ROOT_PKG + catalogFileName);
		XMLCatalogResolver resolver = new XMLCatalogResolver();
		resolver.setCatalogList(new String[] { catalogURL.toString() });
		return resolver;
	}

	/**
	 * Constructs a SchematronValidator that will check an XML resource against the rules
	 * defined in a Schematron schema. An attempt is made to resolve the schema reference
	 * using an entity catalog; if this fails the reference is used as given.
	 * @param schemaRef A reference to a Schematron schema; this is expected to be a
	 * relative or absolute URI value, possibly matching the system identifier for some
	 * entry in an entity catalog.
	 * @param phase The name of the phase to invoke.
	 * @return A SchematronValidator instance, or {@code null} if the validator cannot be
	 * constructed (e.g. invalid schema reference or phase name).
	 */
	public static SchematronValidator buildSchematronValidator(String schemaRef, String phase) {
		Source source = null;
		try {
			String catalogRef = SCH_RESOLVER.resolveSystem(schemaRef);
			if (null != catalogRef) {
				source = new StreamSource(URI.create(catalogRef).toString());
			}
			else {
				source = new StreamSource(schemaRef);
			}
		}
		catch (IOException x) {
			TestSuiteLogger.log(Level.WARNING, "Error reading Schematron schema catalog.", x);
		}
		SchematronValidator validator = null;
		try {
			validator = new SchematronValidator(source, phase);
		}
		catch (Exception e) {
			TestSuiteLogger.log(Level.WARNING, "Error creating Schematron validator.", e);
		}
		return validator;
	}

	/**
	 * Extracts a set of XML Schema references from a source XML document. The document
	 * element is expected to include the standard xsi:schemaLocation attribute.
	 * @param source The source instance to read from; its base URI (systemId) should be
	 * set.
	 * @param baseURI An alternative base URI to use if the source does not have a system
	 * identifier set or if its system id is a {@code file} URI. This will usually be the
	 * URI used to retrieve the resource; it may be null.
	 * @return A Set containing absolute URI references that specify the locations of XML
	 * Schema resources.
	 * @throws javax.xml.stream.XMLStreamException If an error occurs while reading the
	 * source instance.
	 */
	public static Set<URI> extractSchemaReferences(Source source, String baseURI) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader reader = factory.createXMLEventReader(source);
		// advance to document element
		StartElement docElem = reader.nextTag().asStartElement();
		Attribute schemaLoc = docElem
			.getAttributeByName(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"));
		if (null == schemaLoc) {
			throw new RuntimeException("No xsi:schemaLocation attribute found. See ISO 19136, A.3.1.");
		}
		String[] uriValues = schemaLoc.getValue().split("\\s+");
		if (uriValues.length % 2 != 0) {
			throw new RuntimeException("xsi:schemaLocation attribute contains an odd number of URI values:\n"
					+ Arrays.toString(uriValues));
		}
		Set<URI> schemaURIs = new HashSet<URI>();
		// one or more pairs of [namespace name] [schema location]
		for (int i = 0; i < uriValues.length; i += 2) {
			URI schemaURI = null;
			if (!URI.create(uriValues[i + 1]).isAbsolute() && (null != source.getSystemId())) {
				String schemaRef = URIUtils.resolveRelativeURI(source.getSystemId(), uriValues[i + 1]).toString();
				if (schemaRef.startsWith("file") && !new File(schemaRef).exists() && (null != baseURI)) {
					schemaRef = URIUtils.resolveRelativeURI(baseURI, uriValues[i + 1]).toString();
				}
				schemaURI = URI.create(schemaRef);
			}
			else {
				schemaURI = URI.create(uriValues[i + 1]);
			}
			schemaURIs.add(schemaURI);
		}
		return schemaURIs;
	}

	/**
	 * Creates a Schema object representing the complete set of constraints defined in the
	 * CSW 3.0 schema. It incorporates schema components from all relevant namespaces.
	 * @return An immutable Schema object, or <code>null</code> if it cannot be
	 * constructed.
	 */
	public static Schema createCSWSchema() {
		URL entityCatalog = ValidationUtils.class.getResource(ROOT_PKG + "schema-catalog.xml");
		XmlSchemaCompiler xsdCompiler = new XmlSchemaCompiler(entityCatalog);
		Schema appSchema = null;
		try {
			URL schemaRef = ValidationUtils.class.getResource(ROOT_PKG + "xsd/opengis/cat/csw/3.0/csw-3.0.xsd");
			Source xsdSource = new StreamSource(schemaRef.toString());
			appSchema = xsdCompiler.compileXmlSchema(new Source[] { xsdSource });
		}
		catch (SAXException e) {
			TestSuiteLogger.log(Level.WARNING, "Failed to create CSW Schema object.", e);
		}
		return appSchema;
	}

	/**
	 * Creates a Schema object representing the constraints defined in RFC 4287 ("The Atom
	 * Syndication Format"). Appendix B provides an informative RELAX NG grammar (compact
	 * syntax); it can be used to validate either a feed or a stand-alone entry element.
	 * @return An immutable Schema object, or <code>null</code> if it cannot be
	 * constructed.
	 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc4287#appendix-B"> RFC
	 * 4287, Appendix B</a>
	 */
	public static Schema createAtomSchema() {
		SchemaFactory factory = SchemaFactory.newInstance(Constants.RELAXNG_COMPACT_URI, FACTORY_RELAXNG_COMPACT, null);
		URL schemaRef = ValidationUtils.class.getResource(ROOT_PKG + "rnc/atom.rnc");
		Schema schema = null;
		try {
			schema = factory.newSchema(schemaRef);
		}
		catch (SAXException e) {
			TestSuiteLogger.log(Level.WARNING, "Failed to create Atom Schema object from RELAX NG (compact) grammar",
					e);
		}
		return schema;
	}

	/**
	 * Creates a Schema object representing the constraints defined for an OpenSearch
	 * description document (1.1 Draft 5).
	 * @return An immutable Schema object, or <code>null</code> if it cannot be
	 * constructed.
	 * @see <a target="_blank" href=
	 * "http://www.opensearch.org/Specifications/OpenSearch/1.1/Draft_5"> OpenSearch 1.1
	 * Draft 5</a>
	 */
	public static Schema createOpenSearchSchema() {
		SchemaFactory factory = SchemaFactory.newInstance(Constants.RELAXNG_COMPACT_URI, FACTORY_RELAXNG_COMPACT, null);
		URL schemaRef = ValidationUtils.class.getResource(ROOT_PKG + "rnc/osd-1.1-draft5.rnc");
		Schema schema = null;
		try {
			schema = factory.newSchema(schemaRef);
		}
		catch (SAXException e) {
			TestSuiteLogger.log(Level.WARNING,
					"Failed to create OpenSearch Schema object from RELAX NG (compact) grammar", e);
		}
		return schema;
	}

}
