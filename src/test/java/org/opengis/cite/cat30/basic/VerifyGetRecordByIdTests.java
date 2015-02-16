package org.opengis.cite.cat30.basic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class VerifyGetRecordByIdTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    private static Schema cswSchema;
    private static Schema atomSchema;

    @BeforeClass
    public static void initTestFixture() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        Document doc = docBuilder.parse(VerifyGetRecordByIdTests.class.getResourceAsStream(
                "/capabilities-basic.xml"));
        when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
        cswSchema = ValidationUtils.createCSWSchema();
        when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName()))
                .thenReturn(cswSchema);
        atomSchema = ValidationUtils.createAtomSchema();
        when(suite.getAttribute(SuiteAttribute.ATOM_SCHEMA.getName()))
                .thenReturn(atomSchema);
    }

    @Test
    public void getRecordByIdReturnsInvalidAtomEntry() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("schema validation error(s) detected");
        Client client = mock(Client.class);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        WebResource resource = mock(WebResource.class);
        WebResource.Builder builder = mock(WebResource.Builder.class);
        ClientResponse rsp = mock(ClientResponse.class);
        when(resource.accept(any(MediaType.class))).thenReturn(builder);
        when(builder.get(ClientResponse.class)).thenReturn(rsp);
        when(client.resource(any(URI.class))).thenReturn(resource);
        when(resource.queryParams(any(MultivaluedMap.class))).thenReturn(resource);
        Document entity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom/entry-invalid.xml"));
        when(rsp.getStatus()).thenReturn(
                ClientResponse.Status.OK.getStatusCode());
        when(rsp.getEntity(Document.class)).thenReturn(entity);
        GetRecordByIdTests iut = new GetRecordByIdTests();
        iut.initCommonFixture(testContext);
        List<String> idList = new ArrayList<>();
        idList.add("id-01");
        iut.setIdList(idList);
        iut.getRecordByIdAsAtomEntry();
    }

}
