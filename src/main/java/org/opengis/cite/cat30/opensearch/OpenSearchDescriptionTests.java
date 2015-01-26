package org.opengis.cite.cat30.opensearch;

import java.net.URL;
import java.util.logging.Level;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.validation.RelaxNGValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
 * must be approved by the IESG or originate from a recognized standards body.
 * </p>
 *
 * @see
 * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_elements"
 * target="_blank">OpenSearch description elements</a>
 */
public class OpenSearchDescriptionTests {

    private RelaxNGValidator rngValidator;

    /**
     * Builds a Relax NG validator for an OpenSearch description document. The
     * schema resource is located on the classpath at this location:
     * <code>/org/opengis/cite/cat30/rnc/osd-1.1-draft5.rnc</code>.
     *
     */
    @BeforeClass
    public void buildValidator() {
        URL osdSchemaUrl = getClass().getResource(
                "/org/opengis/cite/cat30/rnc/osd-1.1-draft5.rnc");
        try {
            this.rngValidator = new RelaxNGValidator(osdSchemaUrl);
        } catch (Exception ex) {
            TestSuiteLogger.log(Level.WARNING, "buildValidator: ", ex);
        }
    }

    @Test(description = "Test-008")
    public void preferOpenSearchDescription() {
    }

    @Test(description = "Test-021")
    public void getOpenSearchDescription() {
        // fetch osd from base URL
    }

}
