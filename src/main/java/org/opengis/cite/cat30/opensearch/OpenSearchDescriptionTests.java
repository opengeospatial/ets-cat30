package org.opengis.cite.cat30.opensearch;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.validation.RelaxNGValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the structure and content of the OpenSearch description document
 * obtained from the SUT. The document is obtained in response to a GET request
 * submitted to the base service endpoint where the <code>Accept</code> request
 * header expresses a preference for any of the following media types:
 *
 * <ul>
 * <li><code>application/vnd.a9.opensearchdescription+xml</code></li>
 * <li><code>application/opensearchdescription+xml</code></li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> None of the media types listed above appear in the
 * IANA <a href="http://www.iana.org/assignments/media-types/media-types.xhtml"
 * target="_blank">media type registry</a>. Registrations in the standards tree
 * must be approved by the IESG or originate from a recognized standards-related
 * organization (see <a href="http://tools.ietf.org/html/rfc6838#section-3.1"
 * target="_blank">RFC 6838</a>); third-party registrations are allowed in the
 * vendor tree.
 * </p>
 *
 * <h5 style="margin-bottom: 0.5em">Sources</h5>
 * <ul>
 * <li>OGC 12-176r5, 6.4: Obtaining service metadata</li>
 * <li>OGC 12-176r5, 6.5.6: Enabling OpenSearch</li>
 * <li><a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_elements"
 * target="_blank">OpenSearch description elements</a></li>
 * </ul>
 *
 */
public class OpenSearchDescriptionTests extends CommonFixture {

    private RelaxNGValidator osdValidator;
    private URI baseUri;
    private static final String SCHEMATRON_OPENSEARCH_DESCR
            = CommonFixture.ROOT_PKG_PATH + "sch/opensearch-1.1.sch";
    private Document openSearchDescr;

    /**
     * Initializes the test fixture by:
     * <ul>
     * <li>building a Relax NG schema validator for an OpenSearch description
     * document; the schema resource is located on the classpath at this
     * location:
     * <code>/org/opengis/cite/cat30/rnc/osd-1.1-draft5.rnc</code></li>
     * <li>extracting the base GetCapabilities URL (for the GET method binding)
     * from the capabilities document</li>
     * </ul>
     *
     * @param testContext The test context containing various suite attributes.
     *
     */
    @BeforeClass
    public void initFixture(ITestContext testContext) {
        URL schemaUrl = getClass().getResource(CommonFixture.ROOT_PKG_PATH
                + "rnc/osd-1.1-draft5.rnc");
        try {
            this.osdValidator = new RelaxNGValidator(schemaUrl);
        } catch (SAXException | IOException ex) {
            TestSuiteLogger.log(Level.WARNING, getClass().getName(), ex);
        }
        this.openSearchDescr = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.OPENSEARCH_DESCR.getName());
        if (null == this.openSearchDescr) {
            throw new SkipException("OpenSearch description not found in test context.");
        }
    }

    /**
     * [Test] Requests an OpenSearch description document as the most preferred
     * media type. The generic XML media type is included in the Accept header
     * with a q parameter value &lt; 1 ("application/xml; q=0.5").
     */
    @Test(description = "Requirements: 008; Tests: 008")
    public void preferOpenSearchDescription() {
        WebResource resource = this.client.resource(this.baseUri);
        String xmlNotPreferred = MediaType.APPLICATION_XML + "; q=0.5";
        Builder builder = resource.accept(xmlNotPreferred, CAT3.APP_OPENSEARCH_XML);
        Document entity = builder.get(Document.class);
        QName osdDocElemName = new QName(Namespaces.OSD11, "OpenSearchDescription");
        ETSAssert.assertQualifiedName(entity.getDocumentElement(), osdDocElemName);
    }

    /**
     * [Test] Validates the OpenSearch description document obtained from the
     * IUT. The document is checked against the constraints in the OpenSearch
     * 1.1 draft 5 specification.
     *
     * @throws SAXException If the document cannot be read.
     * @throws IOException If an I/O error occurs while trying to access the
     * document.
     *
     * @see "OGC 12-176r5, 6.5.6.5: Requirements for an OpenSearch enabled CSW"
     */
    @Test(description = "Requirements: 021; Tests: 021")
    public void validOpenSearchDescription() throws SAXException, IOException {
        Source entity = new DOMSource(this.openSearchDescr);
        this.osdValidator.validate(entity);
        ValidationErrorHandler err = osdValidator.getErrorHandler();
        Assert.assertFalse(err.errorsDetected(),
                ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                        err.getErrorCount(), err.toString()));
        URL schemaUrl = getClass().getResource(SCHEMATRON_OPENSEARCH_DESCR);
        ETSAssert.assertSchematronValid(schemaUrl, entity);
    }

}
