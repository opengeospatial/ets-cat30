## OGC Catalogue 3.0 Conformance Test Suite

### Scope

The OGC Catalogue 3.0 test suite verifies catalog implementations for conformance 
against the following specifications:

* _OGC Catalogue Services 3.0 Specification - HTTP Protocol Binding_ 
([OGC 12-176r5](https://portal.opengeospatial.org/files/?artifact_id=61521&version=1))
* Catalog Services Specification 3.0 - General Model 
([OGC 12-168r5](https://portal.opengeospatial.org/files/?artifact_id=61522&version=1))
* _OGC Catalogue Services 3.0 Specification - HTTP Protocol Binding - Abstract Test Suite_ 
([OGC 14-014r3](https://portal.opengeospatial.org/files/?artifact_id=61520&version=1))
* _OGC OpenSearch Geo and Time Extensions_ 
([OGC 10-032r8](https://portal.opengeospatial.org/files/?artifact_id=56866&version=2))
* _OpenSearch 1.1_ ([Draft 5](http://www.opensearch.org/Specifications/OpenSearch/1.1))
* _The Atom Syndication Format_ ([RFC 4287](http://tools.ietf.org/html/rfc4287))

Tests for the following conformance classes are currently under development: 

* Basic-Catalogue (mandatory for all implementations)
* OpenSearch

### Plan 2015

The OGC [Testbed 11](http://www.opengeospatial.org/projects/initiatives/testbed11) is funding the development of this test suite. The plan is as follows:

* January -  GetCapabilities - KVP Syntax  and open search
* February - GetRecords 1st draft of the test with query options
* March - May - Refinement of the test 
* June - Submission for TC approval
* Aug - Test available for OGC Certification

### First Implementers and Reference Implemenations

To help develop the test suite, implementations are exercised to test the test suite. Implementations also get improved in the process. The following implementations are participating in the development of the test suite:

- pycsw
- ESRI GeoPortal
- GMU LAITS
- CubeWerx
- GI-cat

### More Information

Visit the [project documentation website](http://opengeospatial.github.io/ets-cat30/) 
for more information, including the API documentation.


### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/ets-cat30/issues) such as a defect or 
an enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/ets-cat30/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add new tests: Fork the repository, implement and verify the tests on a new topic branch, 
and create a pull request (don't forget to periodically rebase long-lived branches)
