/**
 * <p>
 * Includes tests covering capabilities required for <strong>OpenSearch</strong>
 * conformance. The query template parameters listed below must be supported:
 * </p>
 *
 * <ul>
 * <li><code>q={searchTerms}</code></li>
 * <li><code>startPosition={startIndex?}</code></li>
 * <li><code>maxRecords={count?}</code></li>
 * <li><code>bbox={geo:box}</code></li>
 * <li><code>recordIds={geo:uid}</code></li>
 * </ul>
 *
 * <p style="margin-bottom: 0.5em">
 * <strong>Sources</strong>
 * </p>
 * <ul>
 * <li>[OGC-12-176r5] OGC Catalogue Services 3.0 Specification &#x2013; HTTP Protocol
 * Binding, cl. 6.5.6: Enabling OpenSearch</li>
 * <li>[OGC-10-032r8] OGC OpenSearch Geo and Time Extensions, Version 1.0</li>
 * <li>OpenSearch
 * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1" target="_blank">1.1
 * Draft 5</a></li>
 * </ul>
 */
package org.opengis.cite.cat30.opensearch;