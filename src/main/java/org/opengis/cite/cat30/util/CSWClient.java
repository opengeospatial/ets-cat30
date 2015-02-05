package org.opengis.cite.cat30.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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

    public Document getServiceCapabilities() {
        return cswCapabilities;
    }

    public void setServiceCapabilities(Document capabilities) {
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
        MultivaluedMap<String, String> qryParams = new MultivaluedMapImpl();
        qryParams.add(CAT3.REQUEST, CAT3.GET_RECORDS);
        qryParams.add(CAT3.SERVICE, CAT3.SERVICE_TYPE_CODE);
        qryParams.add(CAT3.VERSION, CAT3.SPEC_VERSION);
        qryParams.add(CAT3.MAX_RECORDS, Integer.toString(maxRecords));
        qryParams.add(CAT3.ELEMENT_SET, "full");
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            qryParams.add(CAT3.TYPE_NAMES, "ns1:Record");
            qryParams.add(CAT3.NAMESPACE, "xmlns(ns1=http://www.opengis.net/cat/csw/3.0)");
        }
        WebResource resource = this.client.resource(getRecordsURI).queryParams(qryParams);
        WebResource.Builder builder = resource.accept(mediaType);
        ClientResponse rsp = builder.get(ClientResponse.class);
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
                    "Failed to save GetRecords response to file.", ex);
        }
        return outputFile;
    }
}
