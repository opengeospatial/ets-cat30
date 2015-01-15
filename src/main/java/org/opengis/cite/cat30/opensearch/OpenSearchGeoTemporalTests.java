package org.opengis.cite.cat30.opensearch;

/**
 * Verifies behavior of the SUT when processing queries that contain custom
 * parameters defined in <em>OGC OpenSearch Geo and Time Extensions</em> (OGC
 * 10-032r8).
 *
 * <p>
 * All implementations must satisfy the requirements of the
 * <strong>Core</strong> conformance class. A conforming service must:</p>
 * <ul>
 * <li>present a valid OpenSearch description document;</li>
 * <li>define a URL template for the Atom response type;</li>
 * <li>implement a bounding box search (<code>geo:box</code>).</li>
 * </ul>
 *
 * <p>
 * In addition to the core capabilities listed above, a Catalog v3 service must
 * also enable the <strong>Get record by id</strong> facility that allows a
 * client to retrieve a record by identifier (<code>geo:uid</code>).</p>
 *
 * @see
 * <a href="https://portal.opengeospatial.org/files/?artifact_id=56866&version=2"
 * target="_blank">OGC OpenSearch Geo and Time Extensions, Version 1.0.0</a>
 */
public class OpenSearchGeoTemporalTests {

}
