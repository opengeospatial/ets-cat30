package org.opengis.cite.cat30.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
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
            qryParams.put(CAT3.TYPE_NAMES, "csw:Record");
            qryParams.put(CAT3.NAMESPACE, "xmlns(csw=http://www.opengis.net/cat/csw/3.0)");
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
     * Retrieves an OpenSearch description document from the IUT. The relevant
     * endpoint is the one corresponding to the (mandatory) GET method binding
     * for the GetCapabilities request. The <code>Accept</code> header indicates
     * a preference for the following media types:
     * <ul>
     * <li>{@value org.opengis.cite.cat30.CAT3#APP_VND_OPENSEARCH_XML}</li>
     * <li>{@value org.opengis.cite.cat30.CAT3#APP_OPENSEARCH_XML}</li>
     * </ul>
     *
     * @return A Document representing an OpenSearch description document, or
     * null if one is not available.
     *
     * @see
     * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_document"
     * target="_blank">OpenSearch description document</a>
     */
    public Document getOpenSearchDescription() {
        URI endpoint = ServiceMetadataUtils.getOperationEndpoint(
                this.cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
        ClientRequest req = ClientUtils.buildGetRequest(endpoint, null,
                MediaType.valueOf(CAT3.APP_VND_OPENSEARCH_XML),
                MediaType.valueOf(CAT3.APP_OPENSEARCH_XML));
        ClientResponse rsp = this.client.handle(req);
        if (rsp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
            return null; // probably 404 or 406 if not supported
        }
        Document entityDoc = rsp.getEntity(Document.class);
        if (!entityDoc.getDocumentElement().getNamespaceURI().equals(Namespaces.OSD11)) {
            throw null;
        }
        return entityDoc;
    }
}
