# Release Notes

## 1.0 (2016-12)

* Fix [issue #22](https://github.com/opengeospatial/ets-cat30/issues/22) - Tag 1.0 - Test got approved by OGC
* Fix [issue #21](https://github.com/opengeospatial/ets-cat30/issues/21) - Update documentation with correct version numbers and convert to markdown

## 0.10 (2015-10-30)

*   Fix [issue #17](https://github.com/opengeospatial/ets-cat30/issues/17): Multi-byte characters are not encoded in search term

## 0.9 (2015-09-01)

*   Fix [issue #12](https://github.com/opengeospatial/ets-cat30/issues/12): Write results to user-specified directory
*   Fix [issue #10](https://github.com/opengeospatial/ets-cat30/issues/10): Disable calls to `ETSAssert#assertAllTermsOccur` in simple keyword searches (pending spec clarification)
*   Remove test for maxRecords="unlimited" (pending specification CR)
*   In ETSAssert#assertAllTermsOccur, percent encode search term in assertion error message (it may contain non-ASCII chars).

## 0.8 (2015-07-21)

*   Fix [issues/15](https://github.com/opengeospatial/ets-cat30/issues/15): Allow foreign elements in os:Url
*   Fix [issues/7](https://github.com/opengeospatial/ets-cat30/issues/7): Recognize RSS content in OpenSearch response
*   Fix [issues/6](https://github.com/opengeospatial/ets-cat30/issues/6): Illegal chars in record id
*   Fix [issues/5](https://github.com/opengeospatial/ets-cat30/issues/5): Clean up temp files
*   In BasicGetRecordsTests, change presentSubjectProperty test to presentTitleProperty (verify that no optional elements appear)
*   In ETSAssert.assertAllTermsOccur, also search attributes for matching text
*   In BasicSearchTests, add separate test for maxRecords="unlimited" (REQ-087)

## 0.7 (2015-06-03)

*   New test for GetRecords with multiple search criteria (implicit AND)
*   New tests for GetRecords requests with the `ElementName` parameter (mutually exclusive with `ElementSetName`)
*   New tests for GetRecords requests with the `startPosition` and `maxRecords` parameters
*   Add `ElementSetName` parameter to GetRecords requests as necessary (Requirement-101 to be expunged)
*   Add Schematron schema for Atom feed
*   Recognize georss:where/{http://www.opengis.net/gml}Envelope in Atom feed

## 0.6 (2015-05-20)

This release expands test coverage as follows:

*   Add negative tests for GetRecordById
*   Check operation parameters in capabilities document
*   Add tests for GetCapabilities requests with 'sections' and 'acceptFormats' parameters
*   Add tests covering Requirement-006, -007 (OGC 12-176r6, cl. 6.4)

## 0.5 (2015-04-29)

This release includes the following enhancements:

*   Add tests for basic text search with multiple terms (implicit AND)
*   Verify sample queries specified in OpenSearch description
*   Add test for retrieving OpenSearch description by using the 'OpenSearchDescriptionDocument' constraint.

## 0.4 (2015-04-09)

This release extends the test suite to cover basic text and record searches:

*   Add test for GetRecords (KVP) request with the 'recordIds' query parameter
*   Add tests for GetRecords (KVP) request with the 'q' query parameter
*   Add tests for OpenSearch queries with the searchTerms parameter

## 0.3 (2015-03-31)

This release includes the following enhancements:

*   Add tests for GetRecords with BBOX parameter (bounding box search)
*   Add tests for OpenSearch requests containing the geo:box template parameter (bounding box search)
*   Add tests for OpenSearch requests containing the geo:uid template parameter (record search)

## 0.2 (2015-02-27)

This update adds positive and negative tests for the following requests:

*   GetRecordById
*   GetRecords (without search criteria)

## 0.1 (2015-02-03)

Initial release.

*   Includes basic tests to obtain and validate service metadata resources (service capabilities and OpenSearch description documents).