package org.opengis.cite.cat30;

/**
 * Contains various constants pertaining to Catalogue 3.0 (HTTP) service
 * interfaces as specified in OGC 12-176r5 and related standards.
 * 
 * @see "OGC Catalogue Services 3.0 Specification -- HTTP Protocol Binding, Version 3.0"
 */
public class CAT3 {

    private CAT3() {
    }

    public static final String SCHEMA_URI = "http://schemas.opengis.net/cat/csw/3.0/cswAll.xsd";
    public static final String SERVICE_TYPE_CODE = "CSW";
    public static final String SPEC_VERSION = "3.0.0";
    public static final String GET_CAPABILITIES = "GetCapabilities";
    public static final String GET_RECORD_BY_ID = "GetRecordById";
    public static final String GET_RECORDS = "GetRecords";

    // query parameters
    public static final String REQUEST = "request";
    public static final String SERVICE = "service";
    public static final String VERSION = "version";
    public static final String ACCEPT_VERSIONS = "acceptVersions";
    public static final String ID = "id";

    // exception codes
    public static final String ERR_VER_NEGOTIATION_FAILED = "VersionNegotiationFailed";
}
