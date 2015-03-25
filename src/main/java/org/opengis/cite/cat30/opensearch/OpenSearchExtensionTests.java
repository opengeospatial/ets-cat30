package org.opengis.cite.cat30.opensearch;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.OpenSearchTemplateUtils;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.opengis.cite.geomatics.Extents;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Verifies behavior of the SUT when processing queries that contain custom
 * parameters defined in <em>OGC OpenSearch Geo and Time Extensions</em> (OGC
 * 10-032r8).
 *
 * <p>
 * All implementations must satisfy the requirements of the
 * <strong>Core</strong> conformance class (see OGC 10-032r8, Table 1). A
 * conforming service must:</p>
 * <ul>
 * <li>present a valid OpenSearch description document;</li>
 * <li>define a URL template for the Atom response type;</li>
 * <li>implement a bounding box search (<code>geo:box</code>).</li>
 * </ul>
 *
 * <p>
 * In addition to the service capabilities listed above, a Catalog v3 service
 * must also implement the <strong>Get record by id</strong> conformance class
 * that allows a client to retrieve a record by identifier
 * (<code>geo:uid</code>).</p>
 *
 * @see
 * <a href="https://portal.opengeospatial.org/files/?artifact_id=56866&version=2"
 * target="_blank">OGC OpenSearch Geo and Time Extensions, Version 1.0.0</a>
 */
public class OpenSearchExtensionTests extends CommonFixture {

    private Document openSearchDescr;
    private List<Node> urlTemplates;

    /**
     * Initializes the test fixture. A Document representing an OpenSearch
     * description document is obtained from the test context and the URL
     * templates it contains are extracted.
     *
     * @param testContext The test context containing various suite attributes.
     */
    @BeforeClass
    public void initFixture(ITestContext testContext) {
        this.openSearchDescr = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.OPENSEARCH_DESCR.getName());
        if (null == this.openSearchDescr) {
            throw new SkipException("OpenSearch description not found in test context.");
        }
        this.urlTemplates = ServiceMetadataUtils.getOpenSearchURLTemplates(
                this.openSearchDescr);
    }

    /**
     * [Test] Submits an OpenSearch request that includes a bounding box in the
     * normative CRS ("urn:ogc:def:crs:OGC:1.3:CRS84"). The request URI is
     * constructed in accord with a URL template containing the
     * <code>{geo:box}</code> parameter. Any other template parameters are set
     * to their default values.
     */
    @Test(description = "Requirements: 022,023; OGC 10-032r8, A.3")
    public void boundingBoxQuery() {
        QName bboxParam = new QName(Namespaces.OS_GEO, "box");
        GeneralEnvelope bbox = new GeneralEnvelope(DefaultGeographicCRS.WGS84);
        double[] bboxCoords = new double[]{-123.45, 48.99, -122.45, 49.49};
        bbox.setEnvelope(bboxCoords);
        List<Node> bboxTemplates = OpenSearchTemplateUtils.filterURLTemplatesByParam(
                this.urlTemplates, bboxParam);
        Map<QName, String> values = new HashMap<>();
        values.put(bboxParam, Extents.envelopeToString(bbox));
        for (Node urlTemplate : bboxTemplates) {
            Element url = (Element) urlTemplate;
            String mediaType = url.getAttribute("type");
            URI uri = OpenSearchTemplateUtils.buildRequestURI(url, values);
            ClientRequest req = ClientUtils.buildGetRequest(uri, null,
                    MediaType.valueOf(mediaType));
            ClientResponse rsp = this.client.handle(req);
            Assert.assertEquals(rsp.getStatus(),
                    ClientResponse.Status.OK.getStatusCode(),
                    ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
            Document entity = rsp.getEntity(Document.class);
            QName docElem;
            if (mediaType.startsWith(MediaType.APPLICATION_ATOM_XML)) {
                docElem = new QName(Namespaces.ATOM, "feed");
            } else {
                docElem = new QName(Namespaces.CSW, CAT3.GET_RECORDS_RSP);
            }
            ETSAssert.assertQualifiedName(entity.getDocumentElement(), docElem);
            Source results = new DOMSource(entity);
            ETSAssert.assertEnvelopeIntersectsBoundingBoxes(bbox, results);
        }
    }
}
