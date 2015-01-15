package org.opengis.cite.cat30.opensearch;

/**
 * Verifies behavior of the SUT when processing queries that contain one or more
 * core OpenSearch parameters. The relevant query parameter bindings are shown
 * in the following table.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>Binding OpenSearch query parameters</caption>
 * <thead>
 * <tr>
 * <th>Parameter name</th>
 * <th>Parameter value template</th>
 * <th>Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>q</td>
 * <td>{searchTerms}</td>
 * <td>A comma-separated list of terms used to search across all text fields.
 * The value must be URL-encoded.</td>
 * </tr>
 * <tr>
 * <td>startPosition</td>
 * <td>{startIndex?}</td>
 * <td>Integer value specifying first search result desired by the client.</td>
 * </tr>
 * <tr>
 * <td>maxRecords</td>
 * <td>{count?}</td>
 * <td>Non-negative integer value specifying the number of search results (per
 * page) desired by the client.</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <h6 style="margin-bottom: 0.5em">Sources</h6>
 * <ul>
 * <li>[OGC-12-176r5] OGC Catalogue Services 3.0 Specification &#x2013; HTTP
 * Protocol Binding, Table 6: KVP encoding for query constraints</li>
 * <li>OpenSearch <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1"
 *  target="_blank">1.1 Draft 5</a></li>
 * </ul>
 */
public class OpenSearchCoreTests {

}
