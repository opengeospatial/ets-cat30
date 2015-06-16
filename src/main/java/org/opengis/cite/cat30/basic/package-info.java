/**
 * <p>Includes tests covering capabilities required for <strong>Basic-Catalogue</strong>
 * conformance. The service requests listed below fall into this group; they may 
 * be submitted using either the GET method or the POST method with the content 
 * type "application/x-www-form-urlencoded" (key-value pair syntax).</p>
 * 
 * <ul>
 *   <li><code>GetCapabilities</code>: Obtain information (service-related 
 *   metadata) about the capabilities of the service</li>
 *   <li><code>GetRecordById</code>: Retrieve a representation of a catalog 
 *   record by identifier.</li>
 *   <li><code>GetRecords</code>: Search catalog content and retrieve results 
 *   using basic filter criteria.</li>
 * </ul>
 * 
 * <p>The following presentation formats must be supported:</p>
 *  <ul>
 *   <li>CSW record schema (mostly consisting of DCMI metadata terms);</li>
 *   <li>ATOM syndication format (RFC 4287).</li>
 * </ul>
 *
 * <p style="margin-bottom: 0.5em"><strong>Sources</strong></p>
 * <ul>
 * <li>[OGC-12-176r5] OGC Catalogue Services 3.0 Specification &#x2013; HTTP Protocol 
 * Binding, Table 2: Requirement classes and abstract tests</li>
 * <li>[OGC-14-014r3] OGC Catalogue Services 3.0 Specification &#x2013; HTTP Protocol 
 * Binding &#x2013; Abstract Test Suite, cl. 2: Basic-Catalogue conformance class</li>
 * </ul>
 */
package org.opengis.cite.cat30.basic;
