<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="opensearch-1.1" 
  schemaVersion="3.0.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>CSW 3.0 Capabilities - OpenSearch 1.1 description document</iso:title>

  <iso:ns prefix="osd" uri="http://a9.com/-/spec/opensearch/1.1/" />

  <iso:p>This Schematron schema defines constraints on OpenSearch 1.1 description 
  documents presented by OGC Catalogue 3.0 services.</iso:p>

  <iso:phase id="MainPhase">
    <iso:active pattern="OpenSearchPattern"/>
    <iso:active pattern="SearchResponsePattern"/>
  </iso:phase>

  <iso:pattern id="OpenSearchPattern">
    <iso:rule context="/*[1]">
      <iso:assert test="local-name(.) = 'OpenSearchDescription'" 
        diagnostics="dmsg.local-name">
	  The document element must have [local name] = "OpenSearchDescription".
      </iso:assert>
      <iso:assert test="namespace-uri(.) = 'http://a9.com/-/spec/opensearch/1.1/'" 
        diagnostics="dmsg.ns-name">
        The document element must have [namespace name] = "http://a9.com/-/spec/opensearch/1.1/".
      </iso:assert>
      <iso:assert test="osd:Url[not(@rel) or @rel = 'results']">
	  Missing URL template having @rel = "results" (default)
      </iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="SearchResponsePattern">
    <iso:p>See OGC 12-176r5, Requirement-022, Requirement-023.</iso:p>
    <iso:rule context="osd:OpenSearchDescription">
      <iso:assert 
        test="some $url in osd:Url[not(@rel) or @rel = 'results'] satisfies starts-with($url/@type,'application/atom+xml')">
	  Missing results template with @type = "application/atom+xml" (ATOM-response)
      </iso:assert>
      <iso:assert 
        test="some $url in osd:Url[not(@rel) or @rel = 'results'] satisfies starts-with($url/@type,'application/xml') and contains($url/@template,'outputschema=http://www.opengis.net/cat/csw/3.0')">
        Missing results template with @type = "application/xml" and containing outputschema=http://www.opengis.net/cat/csw/3.0 (CSW-Response)
      </iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="dmsg.local-name" xml:lang="en">
    The root element has [local name] = '<iso:value-of select="local-name(.)"/>'.
    </iso:diagnostic>
    <iso:diagnostic id="dmsg.ns-name" xml:lang="en">
      The element has [namespace name] = '<iso:value-of select="namespace-uri(.)"/>'.
    </iso:diagnostic>
  </iso:diagnostics>

</iso:schema>