/**
 * <p>Includes tests covering capabilities required by the "Basic-Catalogue" 
 * conformance class. The service requests listed below fall into this group; 
 * they may be submitted using either the GET method or the POST method with 
 * the content type "application/x-www-form-urlencoded" (key-value pairs).</p>
 * 
 * <ul>
 *   <li><code>GetCapabilities</code>: Obtain information (service-related 
 *   metadata) about the capabilities of the service</li>
 *   <li><code>GetResourceById</code>: Retrieve a representation of a catalogue 
 *   record by identifier.</li>
 *   <li><code>GetRecords</code>: Retrieve catalogue records using basic filter 
 *   criteria.</li>
 * </ul>
 * 
 * <p>In addition, the following content models must be supported for catalogue 
 * records:</p>
 *  <ul>
 *   <li>CSW record schema (mostly consisting of DCMI metadata terms);</li>
 *   <li>ATOM syndication format (RFC 4287).</li>
 * </ul>
 * 
 * @see "OGC Catalogue Services 3.0 Specification -- HTTP Protocol Binding, 
 * Table 2: Requirement classes and abstract tests
 * @see "OGC Catalogue Services 3.0 Specification -- HTTP Protocol Binding -- 
 * Abstract Test Suite, cl. 2: Basic-Catalogue conformance class"
 */
package org.opengis.cite.cat30.basic;
