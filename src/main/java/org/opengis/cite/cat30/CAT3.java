package org.opengis.cite.cat30;

/**
 * Contains various constants pertaining to Catalogue 3.0 (HTTP) service
 * interfaces as specified in OGC 12-176r5 and related standards.
 *
 * @see "OGC Catalogue Services 3.0 Specification -- HTTP Protocol Binding,
 * Version 3.0"
 */
public class CAT3 {

    private CAT3() {
    }

    public static final String SCHEMA_URI
            = "http://schemas.opengis.net/cat/csw/3.0/cswAll.xsd";
    public static final String SERVICE_TYPE_CODE = "CSW";
    public static final String VERSION_3_0_0 = "3.0.0";
    public static final String GET_CAPABILITIES = "GetCapabilities";
    public static final String GET_RECORD_BY_ID = "GetRecordById";
    public static final String GET_RECORDS = "GetRecords";
    public static final String GET_RECORDS_RSP = "GetRecordsResponse";

    // query parameters
    public static final String REQUEST = "request";
    public static final String SERVICE = "service";
    public static final String VERSION = "version";
    public static final String ACCEPT_VERSIONS = "acceptVersions";
    public static final String ACCEPT_FORMATS = "acceptFormats";
    public static final String ID = "id";
    public static final String MAX_RECORDS = "maxRecords";
    public static final String ELEMENT_SET = "elementSetName";
    public static final String TYPE_NAMES = "typeNames";
    public static final String NAMESPACE = "namespace";
    public static final String OUTPUT_FORMAT = "outputFormat";
    public static final String OUTPUT_SCHEMA = "outputSchema";
    public static final String BBOX = "bbox";
    public static final String Q = "q";
    public static final String REC_ID_LIST = "recordIds";

    // query parameter (code list) values
    public static final String ELEMENT_SET_FULL = "full";
    public static final String ELEMENT_SET_SUMMARY = "summary";
    public static final String ELEMENT_SET_BRIEF = "brief";

    // exception codes: see OGC 06-121r9, Table 27;
    public static final String VER_NEGOTIATION_FAILED = "VersionNegotiationFailed";
    public static final String MISSING_PARAM_VAL = "MissingParameterValue";
    public static final String INVALID_PARAM_VAL = "InvalidParameterValue";
    public static final String INVALID_UPDATE_SEQ = "InvalidUpdateSequence";
    public static final String OPER_NOT_SUPPORTED = "OperationNotSupported";
    public static final String OPT_NOT_SUPPORTED = "OptionNotSupported";

    // media types
    public static final String APP_OPENSEARCH_XML
            = "application/opensearchdescription+xml";
    public static final String APP_VND_OPENSEARCH_XML
            = "application/vnd.a9.opensearchdescription+xml";

    // conformance classes
    public static final String CC_OPEN_SEARCH = "OpenSearch";
}
