package org.opengis.cite.cat30.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.Namespaces;
import org.w3c.dom.Document;

/**
 * A CSW 3.0 client component.
 */
public class CSWClient {

    private static final Logger LOGR = Logger.getLogger(CSWClient.class.getName());
    /**
     * A JAX-RS client.
     */
    private final Client client;
    /**
     * A Document that describes the service under test (csw:Capabilities).
     */
    private Document cswCapabilities;

    public CSWClient() {
        this.client = ClientUtils.buildClient();
    }

    public Document getServiceDescription() {
        return cswCapabilities;
    }

    public void setServiceDescription(Document capabilities) {
        if (!capabilities.getDocumentElement().getNamespaceURI().equals(Namespaces.CSW)) {
            throw new IllegalArgumentException("Expected a CSW v3 capabilities document.");
        }
        this.cswCapabilities = capabilities;
    }

    /**
     * Submits a GetRecords request and saves the response entity to a
     * (temporary) file. The {@value org.opengis.cite.cat30.CAT3#ELEMENT_SET}
     * parameter is set to "full".
     *
     * @param maxRecords The maximum number of records to retrieve.
     * @param mediaType The preferred response media type; this will constitute
     * the value of the Accept header field (generic XML or Atom content).
     *
     * @return A File containing the response entity (csw:GetRecordsResponse);
     * it will be located in the default temporary file directory.
     */
    public File saveFullRecords(final int maxRecords, final MediaType mediaType) {
        URI getRecordsURI = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_RECORDS, HttpMethod.GET);
        Map<String, String> qryParams = new HashMap<>();
        qryParams.put(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.put(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.put(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.put(CAT3.MAX_RECORDS, Integer.toString(maxRecords));
        qryParams.put(CAT3.ELEMENT_SET, CAT3.ELEMENT_SET_FULL);
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            // default namespace is target namespace of default output schema
            qryParams.put(CAT3.TYPE_NAMES, "Record");
        }
        ClientRequest req = ClientUtils.buildGetRequest(getRecordsURI, qryParams,
                mediaType);
        ClientResponse rsp = this.client.handle(req);
        if (rsp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
            return null;
        }
        Document entityDoc = rsp.getEntity(Document.class);
        File outputFile = null;
        try {
            outputFile = File.createTempFile("records-", ".xml");
            FileOutputStream fos = new FileOutputStream(outputFile);
            XMLUtils.writeNode(entityDoc, fos);
            fos.close();
        } catch (IOException ex) {
            LOGR.log(Level.WARNING,
                    "Failed to save GetRecords response entity to file.", ex);
        }
        return outputFile;
    }

    /**
     * Retrieves a complete capabilities document from the specified endpoint.
     *
     * @param uri An absolute URI from which the capabilities can be retrieved;
     * if null, the endpoint from a known capabilities document (which may
     * differ from the one presented by the IUT) is used.
     * @return A Document representing a capabilities document, or null if one
     * is not available.
     */
    public Document getCapabilities(URI uri) {
        if (null == uri || !uri.isAbsolute()) {
            uri = ServiceMetadataUtils.getOperationEndpoint(
                    this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        }
        ClientRequest req = ClientUtils.buildGetRequest(uri, null,
                MediaType.APPLICATION_XML_TYPE);
        ClientResponse rsp = this.client.handle(req);
        Document capabilitiesDoc = null;
        if (rsp.getStatus() == ClientResponse.Status.OK.getStatusCode()
                && XMLUtils.isXML(rsp.getType())) {
            capabilitiesDoc = rsp.getEntity(Document.class);
        }
        return capabilitiesDoc;
    }

    /**
     * Retrieves an OpenSearch description document from the IUT. The default
     * endpoint is the one corresponding to the (mandatory) GET method binding
     * for the GetCapabilities request. The <code>Accept</code> header indicates
     * a preference for the following media types:
     * <ul>
     * <li>{@value org.opengis.cite.cat30.CAT3#APP_VND_OPENSEARCH_XML}</li>
     * <li>{@value org.opengis.cite.cat30.CAT3#APP_OPENSEARCH_XML}</li>
     * </ul>
     * <p>
     * An alternative endpoint may be presented in the capabilities document
     * using the "OpenSearchDescriptionDocument" constraint.
     * </p>
     *
     * @param uri An absolute URI from which the OpenSearch description can be
     * retrieved; if null, the default endpoint is used.
     * @return A Document representing an OpenSearch description document, or
     * null if one is not available.
     *
     * @see
     * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_document"
     * target="_blank">OpenSearch description document</a>
     */
    public Document getOpenSearchDescription(URI uri) {
        if (null == uri || !uri.isAbsolute()) {
            uri = ServiceMetadataUtils.getOperationEndpoint(
                    this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        }
        ClientRequest req = ClientUtils.buildGetRequest(uri, null,
                MediaType.valueOf(CAT3.APP_VND_OPENSEARCH_XML),
                MediaType.valueOf(CAT3.APP_OPENSEARCH_XML));
        ClientResponse rsp = this.client.handle(req);
        if (rsp.getStatus() != ClientResponse.Status.OK.getStatusCode()
                || !XMLUtils.isXML(rsp.getType())) {
            List<String> values = ServiceMetadataUtils.getConstraintValues(
                    cswCapabilities, "OpenSearchDescriptionDocument");
            if (null != values && !values.isEmpty()) {
                URI endpoint = URI.create(values.get(0));
                if (!endpoint.equals(uri)) { // only attempt once
                    return getOpenSearchDescription(endpoint);
                }
            }
            LOGR.config(rsp.toString());
            return null;
        }
        Document entityDoc = rsp.getEntity(Document.class);
        if (!entityDoc.getDocumentElement().getNamespaceURI().equals(Namespaces.OSD11)) {
            LOGR.config(entityDoc.getDocumentElement().getNodeName());
            return null;
        }
        return entityDoc;
    }
}
