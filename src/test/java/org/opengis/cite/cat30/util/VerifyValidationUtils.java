package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.opengis.cite.validation.SchematronValidator;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the ValidationUtils class.
 */
public class VerifyValidationUtils {

	public VerifyValidationUtils() {
	}

	@Test
	public void testBuildSchematronValidator() {
		String schemaRef = "http://schemas.opengis.net/gml/3.2.1/SchematronConstraints.xml";
		String phase = "";
		SchematronValidator result = ValidationUtils.buildSchematronValidator(schemaRef, phase);
		assertNotNull(result);
	}

	@Test
	public void extractRelativeSchemaReference() throws FileNotFoundException, XMLStreamException {
		File xmlFile = new File("src/test/resources/Alpha-1.xml");
		Set<URI> xsdSet = ValidationUtils.extractSchemaReferences(new StreamSource(xmlFile), null);
		URI schemaURI = xsdSet.iterator().next();
		assertTrue("Expected schema reference */xsd/alpha.xsd", schemaURI.toString().endsWith("/xsd/alpha.xsd"));
	}

	@Test
	public void buildAtomSchema() {
		Schema schema = ValidationUtils.createAtomSchema();
		assertNotNull("Failed to construct Atom Schema", schema);
	}

	@Test
	public void validateOpenSearchDescription() throws SAXException, IOException {
		Schema schema = ValidationUtils.createOpenSearchSchema();
		assertNotNull("Failed to build OpenSearch Schema", schema);
		Validator validator = schema.newValidator();
		InputStream inStream = getClass().getResourceAsStream("/opensearch/OpenSearchDescription-valid.xml");
		validator.validate(new StreamSource(inStream));
	}

}
