<?xml version="1.0" encoding="UTF-8"?>
<iso:schema id="csw-capabilities-3.0" 
  schemaVersion="3.0.0"
  xmlns:iso="http://purl.oclc.org/dsdl/schematron" 
  xml:lang="en"
  queryBinding="xslt2">

  <iso:title>CSW 3.0 Capabilities</iso:title>

  <iso:ns prefix="ows" uri="http://www.opengis.net/ows/2.0" />
  <iso:ns prefix="csw" uri="http://www.opengis.net/cat/csw/3.0" />
  <iso:ns prefix="fes" uri="http://www.opengis.net/fes/2.0" />
  <iso:ns prefix="xlink" uri="http://www.w3.org/1999/xlink" />

  <iso:p>This Schematron (ISO 19757-3) schema specifies constraints regarding 
  the content of CSW 3.0 service capabilities descriptions.</iso:p>

  <iso:let name="CSW_NS" value="'http://www.opengis.net/cat/csw/3.0'" />
  <iso:let name="ATOM_NS" value="'http://www.w3.org/2005/Atom'" />

  <iso:phase id="BasicCataloguePhase">
    <iso:active pattern="EssentialCapabilitiesPattern"/>
    <iso:active pattern="TopLevelElementsPattern"/>
    <iso:active pattern="ServiceConstraintsPattern"/>
    <iso:active pattern="ServiceIdentificationPattern"/>
    <iso:active pattern="BasicCataloguePattern"/>
    <iso:active pattern="OperationPattern"/>
  </iso:phase>

  <iso:pattern id="EssentialCapabilitiesPattern">
    <iso:rule context="/*[1]">
      <iso:assert test="local-name(.) = 'Capabilities'" 
        diagnostics="dmsg.local-name">
        The document element must have [local name] = "Capabilities".
      </iso:assert>
      <iso:assert test="namespace-uri(.) = 'http://www.opengis.net/cat/csw/3.0'" 
        diagnostics="dmsg.ns-name">
        The document element must have [namespace name] = "http://www.opengis.net/cat/csw/3.0".
      </iso:assert>
      <iso:assert test="@version = '3.0.0'" diagnostics="dmsg.version.en">
        The capabilities document must have @version = "3.0.0".
      </iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="TopLevelElementsPattern">
    <iso:p>Rules regarding the inclusion of common service metadata elements.</iso:p>
    <iso:rule context="/*[1]">
      <iso:assert test="ows:ServiceIdentification">The ows:ServiceIdentification element is missing.</iso:assert>
      <iso:assert test="ows:ServiceProvider">The ows:ServiceProvider element is missing.</iso:assert>
      <iso:assert test="ows:OperationsMetadata">The ows:OperationsMetadata element is missing.</iso:assert>
      <iso:assert test="ows:Languages">The ows:Languages element is missing.</iso:assert>
      <iso:assert test="fes:Filter_Capabilities">The fes:Filter_Capabilities element is missing.</iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="ServiceConstraintsPattern">
    <iso:p>Implementation conformance statement. See OGC 12-176r6: Table 17.</iso:p>
    <iso:rule id="R48" context="ows:OperationsMetadata">
      <iso:assert test="ows:Constraint[ends-with(@name,'OpenSearch')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'OpenSearch')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/OpenSearch'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetCapabilities-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetCapabilities-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetCapabilities-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetRecordById-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetRecordById-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetRecordById-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetRecords-Basic-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetRecords-Basic-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetRecords-Basic-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetRecords-Distributed-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetRecords-Distributed-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetRecords-Distributed-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetRecords-Distributed-KVP')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetRecords-Distributed-KVP')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetRecords-Distributed-KVP'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetRecords-Async-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetRecords-Async-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetRecords-Async-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetRecords-Async-KVP')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetRecords-Async-KVP')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetRecords-Async-KVP'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetDomain-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetDomain-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetDomain-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'GetDomain-KVP')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'GetDomain-KVP')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/GetDomain-KVP'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Transaction')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Transaction')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Transaction'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Harvest-Basic-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Harvest-Basic-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Harvest-Basic-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Harvest-Basic-KVP')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Harvest-Basic-KVP')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Harvest-Basic-KVP'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Harvest-Async-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Harvest-Async-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Harvest-Async-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Harvest-Async-KVP')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Harvest-Async-KVP')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Harvest-Async-KVP'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Harvest-Periodic-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Harvest-Periodic-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Harvest-Periodic-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Harvest-Periodic-KVP')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Harvest-Periodic-KVP')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Harvest-Periodic-KVP'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Filter-CQL')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Filter-CQL')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Filter-CQL'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Filter-FES-XML')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Filter-FES-XML')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Filter-FES-XML'.
      </iso:assert>
      <iso:assert test="ows:Constraint[ends-with(@name,'Filter-FES-KVP-Advanced')]/ows:DefaultValue or 
      ows:Constraint[ends-with(@name,'Filter-FES-KVP-Advanced')]//ows:Value">
      No ows:Constraint value found for conformance class 'http://www.opengis.net/spec/csw/3.0/conf/Filter-FES-KVP-Advanced'.
      </iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="BasicCataloguePattern">
    <iso:p>Basic-Catalogue conformance class. See OGC 12-176r6: Table 1.</iso:p>
    <iso:rule context="ows:OperationsMetadata">
      <iso:assert test="ows:Operation[@name='GetCapabilities']//ows:Get/@xlink:href">
      The GET method endpoint for GetCapabilities is missing.
      </iso:assert>
      <iso:assert test="ows:Operation[@name='GetRecordById']">
      The mandatory GetRecordById operation is missing.
      </iso:assert>
      <iso:assert test="ows:Operation[@name='GetRecords']">
      The mandatory GetRecords operation is missing.
      </iso:assert>
    </iso:rule>
    <iso:rule context="fes:Filter_Capabilities/fes:Conformance">
      <iso:assert test="upper-case(fes:Constraint[@name='ImplementsMinSpatialFilter']/ows:DefaultValue) = 'TRUE'">
      The filter conformance constraint 'ImplementsMinSpatialFilter' must be 'TRUE' (see OGC 09-026r1).
      </iso:assert>
    </iso:rule>
    <iso:rule context="fes:Filter_Capabilities/fes:Conformance">
      <iso:assert test="upper-case(fes:Constraint[@name='ImplementsMinimumXPath']/ows:DefaultValue) = 'TRUE'">
      The filter conformance constraint 'ImplementsMinimumXPath' must be 'TRUE' (see OGC 09-026r1).
      </iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="OperationPattern">
    <iso:p>Constraints that apply to Operation elements.</iso:p>
    <iso:rule id="R137-R138" context="ows:Operation[@name='GetRecords']">
      <iso:assert test="ows:Parameter[matches(@name,'outputSchema','i')]//ows:Value[1] eq $CSW_NS">
      GetRecords: the first (default) value of the outputSchema parameter must be '<iso:value-of select="$CSW_NS"/>'  (7.3.4.4).
      </iso:assert>
      <iso:assert test="ows:Parameter[matches(@name,'outputSchema','i')]//ows:Value = $ATOM_NS">
      GetRecords: outputSchema parameter must allow '<iso:value-of select="$ATOM_NS"/>' (7.3.4.4).
      </iso:assert>
      <iso:assert test="ows:Parameter[matches(@name,'bbox','i')]">
      GetRecords: 'bbox' parameter is missing (Table 6, Bounding box search).
      </iso:assert>
      <iso:assert test="ows:Parameter[matches(@name,'recordIds','i')]">
      GetRecords: 'recordIds' parameter is missing (Table 6, Record search).
      </iso:assert>
      <iso:assert test="ows:Parameter[matches(@name,'q','i')]">
      GetRecords: 'q' parameter is missing (Table 6, Text search).
      </iso:assert>
    </iso:rule>
    <iso:rule id="R137-R138" context="ows:Operation[@name='GetRecordById']">
      <iso:assert test="ows:Parameter[matches(@name,'outputSchema','i')]//ows:Value[1] eq $CSW_NS">
      GetRecordById: the first (default) value of the outputSchema parameter must be '<iso:value-of select="$CSW_NS"/>' (7.4.4.4).
      </iso:assert>
      <iso:assert test="ows:Parameter[matches(@name,'outputSchema','i')]//ows:Value = $ATOM_NS">
      GetRecordById: outputSchema parameter must allow '<iso:value-of select="$ATOM_NS"/>' (7.4.4.4).
      </iso:assert>
    </iso:rule>
  </iso:pattern>

  <iso:pattern id="ServiceIdentificationPattern">
    <iso:rule context="ows:ServiceIdentification">
      <iso:assert test="upper-case(ows:ServiceType) = 'CSW'"
        diagnostics="dmsg.serviceType.en"> 
        The value of the ows:ServiceType element must be "CSW".
      </iso:assert>
      <iso:assert test="ows:ServiceTypeVersion = '3.0.0'" 
        diagnostics="dmsg.serviceTypeVersion.en">
        An ows:ServiceTypeVersion element having the value "3.0.0" must be present.
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
    <iso:diagnostic id="dmsg.version.en" xml:lang="en">
    The reported version is <iso:value-of select="/*[1]/@version"/>.
    </iso:diagnostic>
    <iso:diagnostic id="dmsg.serviceType.en" xml:lang="en">
    The reported ServiceType is '<iso:value-of select="./ows:ServiceType"/>'.
    </iso:diagnostic>
    <iso:diagnostic id="dmsg.serviceTypeVersion.en" xml:lang="en">
    The reported ServiceTypeVersion is <iso:value-of select="./ows:ServiceTypeVersion"/>.
    </iso:diagnostic>
  </iso:diagnostics>

</iso:schema>