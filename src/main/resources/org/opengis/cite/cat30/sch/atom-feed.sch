<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="atom-feed" 
  schemaVersion="3.0.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>Constraints on Atom feeds for CSW 3.0 catalogue services</iso:title>

  <iso:ns prefix="atom" uri="http://www.w3.org/2005/Atom" />
  <iso:ns prefix="os" uri="http://a9.com/-/spec/opensearch/1.1/" />

  <iso:p>A Schematron schema that defines constraints on Atom feeds presented 
  by CSW 3.0 catalogue implementations.</iso:p>

  <iso:phase id="MainPhase">
    <iso:active pattern="AtomFeedPattern"/>
  </iso:phase>

  <iso:pattern id="AtomFeedPattern">
    <iso:p>See OGC 10-032r8: 9.3.2, Table 6</iso:p>
    <iso:rule context="/">
      <iso:assert test="atom:feed" diagnostics="msg.root.en">
      Expected atom:feed as document (root) element.
      </iso:assert>
    </iso:rule>
    <iso:rule context="atom:feed">
      <iso:assert test="atom:author">atom:author not found in atom:feed</iso:assert>
      <iso:assert test="atom:link[@rel='search' and contains(@type,'opensearchdescription+xml')]">
      atom:link that refers to an OpenSearch description not found in atom:feed (autodiscovery link).
      </iso:assert>
      <iso:assert test="os:totalResults">os:totalResults not found in atom:feed</iso:assert>
      <iso:assert test="os:startIndex">os:startIndex not found in atom:feed</iso:assert>
      <iso:assert test="os:itemsPerPage">os:itemsPerPage not found in atom:feed</iso:assert>
      <iso:assert test="os:Query[@role='request']">os:Query[@role='request'] not found in atom:feed</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:diagnostics>
    <iso:diagnostic id="msg.root.en" xml:lang="en">
    Element has [local name] = '<iso:value-of select="local-name(/*[1])"/>' and [namespace name] = '<iso:value-of select="namespace-uri(/*[1])"/>'.
    </iso:diagnostic>
  </iso:diagnostics>
</iso:schema>
