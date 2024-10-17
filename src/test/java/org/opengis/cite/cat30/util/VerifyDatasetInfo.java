package org.opengis.cite.cat30.util;

import java.io.File;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.Envelope;

/**
 * Verifies the behavior of the DatasetInfo class.
 */
public class VerifyDatasetInfo {

	private static DocumentBuilder docBuilder;

	public VerifyDatasetInfo() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void getGeographicExtent() throws URISyntaxException {
		URL url = getClass().getResource("/rsp/GetRecordsResponse-full.xml");
		File dataFile = new File(url.toURI());
		DatasetInfo dataset = new DatasetInfo(dataFile);
		Envelope env = dataset.getGeographicExtent();
		assertEquals("Unexpected CRS code", "WGS 84", env.getCoordinateReferenceSystem().getName().getCode());
		assertArrayEquals("Unexpected coords for upper corner.", new double[] { 33.63, -116.00 },
				env.getUpperCorner().getCoordinate(), 0.005);
	}

	@Test
	public void getRecordIdentifiers() throws URISyntaxException {
		URL url = getClass().getResource("/rsp/GetRecordsResponse-full.xml");
		File dataFile = new File(url.toURI());
		DatasetInfo dataset = new DatasetInfo(dataFile);
		List<String> idList = dataset.getRecordIdentifiers();
		assertEquals("Unexpected number of identifiers", 10, idList.size());
		ListIterator<String> listItr = idList.listIterator(idList.size());
		String lastId = listItr.previous();
		assertEquals("Unexpected value for last id.", "urn:uuid:b1254954-f765-11e1-bf69-aa0000ae6bfc", lastId);
	}

	@Test
	public void calculateExtentUsingHttpCRSReferences() throws URISyntaxException {
		URL url = getClass().getResource("/rsp/GetRecordsResponse-3.xml");
		File dataFile = new File(url.toURI());
		DatasetInfo dataset = new DatasetInfo(dataFile);
		Envelope env = dataset.getGeographicExtent();
		assertEquals("Unexpected CRS code", "WGS 84", env.getCoordinateReferenceSystem().getName().getCode());
		assertArrayEquals("Unexpected coords for upper corner.", new double[] { 68.41, 17.92 },
				env.getUpperCorner().getCoordinate(), 0.005);
	}

}
