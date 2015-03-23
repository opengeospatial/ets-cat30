package org.opengis.cite.cat30;

import com.sun.jersey.api.client.Client;
import java.io.File;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a
 * shared test fixture.
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
     * A file containing records retrieved from the IUT
     * (csw:GetRecordsResponse).
     */
    DATA_FILE("dataFile", File.class),
    /**
     * A DOM Document representing the test subject or a description of it.
     */
    TEST_SUBJECT("testSubject", Document.class),
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

    public Class getType() {
        return attrType;
    }

    public String getName() {
        return attrName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(attrName);
        sb.append('(').append(attrType.getName()).append(')');
        return sb.toString();
    }
}
