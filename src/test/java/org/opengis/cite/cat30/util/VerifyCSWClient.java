package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Ignore;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jakarta.ws.rs.core.MediaType;

/**
 * Verifies the behavior of the CSWClient class.
 */
public class VerifyCSWClient {

	private static DocumentBuilder docBuilder;

	@BeforeClass
	public static void initFixture() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	@Ignore
	public void saveFullRecordsAsCsw() throws SAXException, IOException {
		CSWClient iut = new CSWClient();
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities-pycsw-cite.xml"));
		iut.setServiceDescription(doc);
		File file = iut.saveFullRecords(10, MediaType.APPLICATION_XML_TYPE);
		assertTrue("Response file does not exist: " + file.getAbsolutePath(), file.exists());
	}

}
