# OGC Catalogue 3.0 Conformance Test Suite

## Scope

This executable test suite (ETS) verifies the behavior of implementations with respect to following specifications:

![Set of relevant specifications](img/specifications.png)
**Figure 1** - Relevant specifications

The documents listed below stipulate requirements that must be satisfied by a conforming implementation.

*   [OGC 12-176r7](http://docs.opengeospatial.org/is/12-176r7/12-176r7.html) OGC Catalogue Services 3.0 Specification – HTTP Protocol Binding
*   [OGC 12-168r6](http://docs.opengeospatial.org/is/12-168r6/12-168r6.html) Catalog Services Specification 3.0 – General Model
*   [OGC 14-014r3](https://portal.opengeospatial.org/files/?artifact_id=61520%26version=1) OGC Catalogue Services 3.0 Specification – HTTP Protocol Binding – Abstract Test Suite
*   [OGC 10-032r8](https://portal.opengeospatial.org/files/?artifact_id=56866%26version=2) OGC OpenSearch Geo and Time Extensions, Version 1.0
*   [OpenSearch](http://www.opensearch.org/Specifications/OpenSearch/1.1) OpenSearch 1.1, Draft 5
*   [RFC 4287](http://tools.ietf.org/html/rfc4287) The Atom Syndication Format



There are 24 conformance classes defined in the principal specification (OGC 12-176r5); only a subset of these are covered by this test suite:

**Basic-Catalogue**

Basic search and retrieval functionality required of all implementations; there are several subsidiary conformance classes that introduce additional requirements:

*   Filter-FES-KVP
*   CSW-response
*   ATOM-response

**OpenSearch**

Supports OpenSearch 1.1 parameters, including extensions for simple spatial (bounding box) queries.

## Test requirements

The test requires a valid URL that points to a CSW 3.0 capabilities document.


## Test suite structure

The test suite definition file (testng.xml) is located in the root package, `org.opengis.cite.cat30`. A conformance class corresponds to a *test* element; each test element includes a set of test classes that define the actual test methods. The general structure of the test suite is as follows:

| Conformance class | Test classes in the Java code |
|--- | --- | 
| Basic-Catalogue | org.opengis.cite.cat30.basic |
| OpenSearch | org.opengis.cite.cat30.opensearch |

The [Javadoc documentation](apidocs/index.html) provides more detailed information about the test methods that constitute this test executable suite.


## How to run the tests

The test suite may be run in any of the following environments:

*   Integrated development environment (IDE): The main Java class is `org.opengis.cite.cat30.TestNGController`. The first argument must refer to an XML properties file containing the required test run argument (a reference to a CSW 3.0 capabilities document). If not specified, the default location at ${user.home}/test-run-props.xml will be used. You can modify the sample file in src/main/config/test-run-props.xml. The TestNG results file (testng-results.xml) will be written to a subdirectory in ${user.home}/testng/ having a UUID value as its name.
*   RESTful API: Submit a request that includes the required arguments to the test run controller (/rest/suites/cat30/${project.version}/run).
*   Command shell (console): Execute the "all-in-one" JAR file:

    <pre>java -jar ets-cat30-${version}-aio.jar [-o|--outputDir $TMPDIR] [test-run-props.xml]</pre>

    .
*   TEAM-Engine: Run the CTL script located in the `/src/main/ctl/` directory.

The test run arguments are summarized in Table 2\. The _Obligation_ descriptor can have the following values: M (mandatory), O (optional), or C (conditional).

| Name | Value domain  | Obligation | Description |
|--- | --- | --- | --- | 
| iut | URI | M |A URI that refers to a service capabilities document (csw:Capabilities) that describes the implementation under test. Ampersand (%26) characters must be percent-encoded as '%26'.

More information at the [users guide page](http://opengeospatial.github.io/teamengine/users.html).