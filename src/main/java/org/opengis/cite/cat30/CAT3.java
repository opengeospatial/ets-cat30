package org.opengis.cite.cat30;

/**
 * Contains various constants pertaining to Catalogue 3.0 (HTTP) service interfaces as
 * specified in OGC 12-176r5 and related standards.
 *
 * @see "OGC Catalogue Services 3.0 Specification -- HTTP Protocol Binding, Version 3.0"
 */
public class CAT3 {

	private CAT3() {
	}

	/** Constant <code>SCHEMA_URI="http://schemas.opengis.net/cat/csw/3.0/"{trunked}</code> */
	public static final String SCHEMA_URI = "http://schemas.opengis.net/cat/csw/3.0/cswAll.xsd";

	/** Constant <code>SERVICE_TYPE_CODE="CSW"</code> */
	public static final String SERVICE_TYPE_CODE = "CSW";

	/** Constant <code>VERSION_3_0_0="3.0.0"</code> */
	public static final String VERSION_3_0_0 = "3.0.0";

	/** Constant <code>GET_CAPABILITIES="GetCapabilities"</code> */
	public static final String GET_CAPABILITIES = "GetCapabilities";

	/** Constant <code>GET_RECORD_BY_ID="GetRecordById"</code> */
	public static final String GET_RECORD_BY_ID = "GetRecordById";

	/** Constant <code>GET_RECORDS="GetRecords"</code> */
	public static final String GET_RECORDS = "GetRecords";

	/** Constant <code>GET_RECORDS_RSP="GetRecordsResponse"</code> */
	public static final String GET_RECORDS_RSP = "GetRecordsResponse";

	/** Constant <code>SEARCH_RESULTS="SearchResults"</code> */
	public static final String SEARCH_RESULTS = "SearchResults";

	// request parameters
	/** Constant <code>REQUEST="request"</code> */
	public static final String REQUEST = "request";

	/** Constant <code>SERVICE="service"</code> */
	public static final String SERVICE = "service";

	/** Constant <code>VERSION="version"</code> */
	public static final String VERSION = "version";

	/** Constant <code>ACCEPT_VERSIONS="acceptVersions"</code> */
	public static final String ACCEPT_VERSIONS = "acceptVersions";

	/** Constant <code>ACCEPT_FORMATS="acceptFormats"</code> */
	public static final String ACCEPT_FORMATS = "acceptFormats";

	/** Constant <code>SECTIONS="sections"</code> */
	public static final String SECTIONS = "sections";

	/** Constant <code>ID="id"</code> */
	public static final String ID = "id";

	/** Constant <code>MAX_RECORDS="maxRecords"</code> */
	public static final String MAX_RECORDS = "maxRecords";

	/** Constant <code>ELEMENT_SET="elementSetName"</code> */
	public static final String ELEMENT_SET = "elementSetName";

	/** Constant <code>ELEMENT_NAME="elementName"</code> */
	public static final String ELEMENT_NAME = "elementName";

	/** Constant <code>TYPE_NAMES="typeNames"</code> */
	public static final String TYPE_NAMES = "typeNames";

	/** Constant <code>NAMESPACE="namespace"</code> */
	public static final String NAMESPACE = "namespace";

	/** Constant <code>OUTPUT_FORMAT="outputFormat"</code> */
	public static final String OUTPUT_FORMAT = "outputFormat";

	/** Constant <code>OUTPUT_SCHEMA="outputSchema"</code> */
	public static final String OUTPUT_SCHEMA = "outputSchema";

	/** Constant <code>START_POS="startPosition"</code> */
	public static final String START_POS = "startPosition";

	/** Constant <code>BBOX="bbox"</code> */
	public static final String BBOX = "bbox";

	/** Constant <code>Q="q"</code> */
	public static final String Q = "q";

	/** Constant <code>REC_ID_LIST="recordIds"</code> */
	public static final String REC_ID_LIST = "recordIds";

	// response properties
	/** Constant <code>NUM_REC_RETURNED="numberOfRecordsReturned"</code> */
	public static final String NUM_REC_RETURNED = "numberOfRecordsReturned";

	/** Constant <code>NUM_REC_MATCHED="numberOfRecordsMatched"</code> */
	public static final String NUM_REC_MATCHED = "numberOfRecordsMatched";

	/** Constant <code>NEXT_REC="nextRecord"</code> */
	public static final String NEXT_REC = "nextRecord";

	// query parameter (code list) values
	/** Constant <code>ELEMENT_SET_FULL="full"</code> */
	public static final String ELEMENT_SET_FULL = "full";

	/** Constant <code>ELEMENT_SET_SUMMARY="summary"</code> */
	public static final String ELEMENT_SET_SUMMARY = "summary";

	/** Constant <code>ELEMENT_SET_BRIEF="brief"</code> */
	public static final String ELEMENT_SET_BRIEF = "brief";

	// exception codes: see OGC 06-121r9, Table 27;
	/** Constant <code>VER_NEGOTIATION_FAILED="VersionNegotiationFailed"</code> */
	public static final String VER_NEGOTIATION_FAILED = "VersionNegotiationFailed";

	/** Constant <code>MISSING_PARAM_VAL="MissingParameterValue"</code> */
	public static final String MISSING_PARAM_VAL = "MissingParameterValue";

	/** Constant <code>INVALID_PARAM_VAL="InvalidParameterValue"</code> */
	public static final String INVALID_PARAM_VAL = "InvalidParameterValue";

	/** Constant <code>INVALID_UPDATE_SEQ="InvalidUpdateSequence"</code> */
	public static final String INVALID_UPDATE_SEQ = "InvalidUpdateSequence";

	/** Constant <code>OPER_NOT_SUPPORTED="OperationNotSupported"</code> */
	public static final String OPER_NOT_SUPPORTED = "OperationNotSupported";

	/** Constant <code>OPT_NOT_SUPPORTED="OptionNotSupported"</code> */
	public static final String OPT_NOT_SUPPORTED = "OptionNotSupported";

	/** Constant <code>NO_CODE="NoApplicableCode"</code> */
	public static final String NO_CODE = "NoApplicableCode";

	/** Constant <code>PROCESSING_FAILED="OperationProcessingFailed"</code> */
	public static final String PROCESSING_FAILED = "OperationProcessingFailed";

	/** Constant <code>PARSING_FAILED="OperationParsingFailed"</code> */
	public static final String PARSING_FAILED = "OperationParsingFailed";

	// media types
	/** Constant <code>APP_OPENSEARCH_XML="application/opensearchdescription+xml"</code> */
	public static final String APP_OPENSEARCH_XML = "application/opensearchdescription+xml";

	/** Constant <code>APP_VND_OPENSEARCH_XML="application/vnd.a9.opensearchdescriptio"{trunked}</code> */
	public static final String APP_VND_OPENSEARCH_XML = "application/vnd.a9.opensearchdescription+xml";

	// conformance classes
	/** Constant <code>CC_OPEN_SEARCH="OpenSearch"</code> */
	public static final String CC_OPEN_SEARCH = "OpenSearch";

}
