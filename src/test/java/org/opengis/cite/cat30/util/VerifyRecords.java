package org.opengis.cite.cat30.util;

import static org.junit.Assert.*;

import java.io.File;

import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.namespace.QName;
import net.sf.saxon.s9api.XdmValue;

import org.junit.Test;
import org.opengis.cite.cat30.Namespaces;

/**
 * Verifies the behavior of the Records class.
 */
public class VerifyRecords {

	public VerifyRecords() {
	}

	@Test
	public void findRecordsWithSubject() throws URISyntaxException {
		URL url = getClass().getResource("/rsp/GetRecordsResponse-full.xml");
		File dataFile = new File(url.toURI());
		QName subject = new QName(Namespaces.DCMES, "subject");
		XdmValue results = Records.findRecordsInSampleData(dataFile, subject);
		assertEquals("Unexpected number of results", 5, results.size());
	}

	@Test
	public void findRecordsWithTitleAndLanguage() throws URISyntaxException {
		URL url = getClass().getResource("/rsp/GetRecordsResponse-full.xml");
		File dataFile = new File(url.toURI());
		QName title = new QName(Namespaces.DCMES, "title");
		QName lang = new QName(Namespaces.DCMES, "language");
		XdmValue results = Records.findRecordsInSampleData(dataFile, title, lang);
		assertEquals("Unexpected number of results", 10, results.size());
	}

}
