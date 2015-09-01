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

The test suite currently covers the following conformance classes:

* Basic-Catalogue (mandatory for all implementations)
* OpenSearch

Visit the [project documentation website](http://opengeospatial.github.io/ets-cat30/) 
for more information, including the API documentation.

### How to run the tests
There are several options for executing the test suite.

#### 1. OGC test harness

Use [TEAM Engine](https://github.com/opengeospatial/teamengine), the official OGC test harness.
The latest test suite release should be available at the [beta testing facility](http://cite.opengeospatial.org/te2/). 
You can also [build and deploy](https://github.com/opengeospatial/teamengine) the test 
harness yourself and use a local installation.

#### 2. Integrated development environment (IDE)
Use a Java IDE such as Eclipse, NetBeans, or IntelliJ.
Clone the repository and build the project.

Set the main class to run: `org.opengis.cite.cat30.TestNGController`

Arguments: The first argument must refer to an XML properties file containing the 
required test run argument (a reference to a CSW 3.0 capabilities document). If 
not specified, the default location at `${user.home}/test-run-props.xml` will be 
used.
   
You can modify the sample file in `src/main/config/test-run-props.xml`

```xml   
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties version="1.0">
  <comment>Test run arguments (ets-cat30)</comment>
  <entry key="iut">http://demo.pycsw.org/cite/csw?service=CSW&amp;request=GetCapabilities</entry>
</properties>
```

The TestNG results file (`testng-results.xml`) will be written to a subdirectory
in `${user.home}/testng/` having a UUID value as its name.

#### 3. Command shell (console)

One of the build artifacts is an "all-in-one" JAR file that includes the test 
suite and all of its dependencies; this makes it very easy to execute the test 
suite in a command shell:

`java -jar ets-cat30-${version}-aio.jar [-o|--outputDir $TMPDIR] [test-run-props.xml]`

### How to contribute

If you would like to get involved, you can:

* [Report an issue](https://github.com/opengeospatial/ets-cat30/issues) such as a defect or 
an enhancement request
* Help to resolve an [open issue](https://github.com/opengeospatial/ets-cat30/issues?q=is%3Aopen)
* Fix a bug: Fork the repository, apply the fix, and create a pull request
* Add new tests: Fork the repository, implement and verify the tests on a new topic branch, 
and create a pull request (don't forget to periodically rebase long-duration branches)

-----
### Plan 2015

The OGC [Testbed 11](http://www.opengeospatial.org/projects/initiatives/testbed11) funded 
the development of this test suite. The goal is to make it available for OGC certification 
in late summer.

### First Implementers and Reference Implementations

To help develop the test suite, implementations are exercised to test the test suite. 
Implementations also get improved in the process. The following implementations are 
participating in the development of the test suite:

- [pycsw](http://pycsw.org/)
- ESRI GeoPortal
- GMU LAITS
- CubeWerx
- GI-cat
