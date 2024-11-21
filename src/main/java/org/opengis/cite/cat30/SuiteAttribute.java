package org.opengis.cite.cat30;

import java.io.File;

import javax.xml.validation.Schema;

import org.opengis.cite.cat30.util.DatasetInfo;
import org.w3c.dom.Document;

import jakarta.ws.rs.client.Client;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a shared
 * test fixture.
 */
@SuppressWarnings("rawtypes")
public enum SuiteAttribute {

	/**
	 * A client component for interacting with HTTP endpoints.
	 */
	CLIENT("httpClient", Client.class),
	/**
	 * An immutable Schema object representing the complete CSW 3.0 schema.
	 */
	CSW_SCHEMA("cswSchema", Schema.class),
	/**
	 * An immutable Schema object for Atom (RFC 4287).
	 */
	ATOM_SCHEMA("atomSchema", Schema.class),
	/**
	 * Sample data obtained from the IUT.
	 */
	DATASET("dataset", DatasetInfo.class),
	/**
	 * A DOM Document representing the test subject or a description of it.
	 */
	TEST_SUBJECT("testSubject", Document.class),
	/**
	 * A File containing the test subject or a description of it.
	 */
	TEST_SUBJ_FILE("testSubjectFile", File.class),
	/**
	 * A DOM Document representing an OpenSearch 1.1 description.
	 */
	OPENSEARCH_DESCR("openSearchDescr", Document.class);

	private final Class attrType;

	private final String attrName;

	private SuiteAttribute(String attrName, Class attrType) {
		this.attrName = attrName;
		this.attrType = attrType;
	}

	/**
	 * <p>getType.</p>
	 *
	 * @return a {@link java.lang.Class} object
	 */
	public Class getType() {
		return attrType;
	}

	/**
	 * <p>getName.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getName() {
		return attrName;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(attrName);
		sb.append('(').append(attrType.getName()).append(')');
		return sb.toString();
	}

}
