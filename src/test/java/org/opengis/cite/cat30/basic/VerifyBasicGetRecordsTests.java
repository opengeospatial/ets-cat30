package org.opengis.cite.cat30.basic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.TestCommon;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class VerifyBasicGetRecordsTests extends TestCommon {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static DocumentBuilder docBuilder;

    private static ITestContext testContext;

    private static Schema cswSchema;

    private static Schema atomSchema;
    
    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        testContext = mock( ITestContext.class );
        when( testContext.getSuite() ).thenReturn( suite );
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        docBuilder = dbf.newDocumentBuilder();
        cswSchema = ValidationUtils.createCSWSchema();
        when( suite.getAttribute( SuiteAttribute.CSW_SCHEMA.getName() ) ).thenReturn( cswSchema );
        atomSchema = ValidationUtils.createAtomSchema();
        when( suite.getAttribute( SuiteAttribute.ATOM_SCHEMA.getName() ) ).thenReturn( atomSchema );
    }

    @Test
    public void testPresentTitleProperty() throws Exception {
        Document doc = docBuilder.parse(getClass().getResourceAsStream("/capabilities/basic.xml"));
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( doc );
        mockResponse();
        Document entity =
                docBuilder.parse(this.getClass().getResourceAsStream("/getrecords/GetRecords-Summary-Response.xml"));
        BasicGetRecordsTests spy = Mockito.spy(new BasicGetRecordsTests());
        spy.setGetEndpoint(new URI("http://test"));
        try (MockedStatic<ClientUtils> clientUtils = Mockito.mockStatic(ClientUtils.class)) {
            clientUtils.when(() -> ClientUtils.buildGetRequest(any(URI.class), ArgumentMatchers.<String, String>anyMap(), any(MediaType.class)))
                    .thenReturn(rsp);
            Mockito.doReturn(entity).when(spy).getResponseEntityAsDocument(any(Response.class), nullable(String.class));
            spy.initCommonFixture(testContext);
            spy.presentTitleProperty();
        }
    }

    @Test
    public void testPresentTitleProperty_invalid()
                            throws Exception {
        thrown.expect(AssertionError.class);
        Document doc = docBuilder.parse( getClass().getResourceAsStream( "/capabilities/basic.xml" ) );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( doc );
        mockResponse();
        Document entity = docBuilder.parse( this.getClass().getResourceAsStream( "/getrecords/GetRecords-Summary-Response-invalid.xml" ) );
        BasicGetRecordsTests spy = Mockito.spy( new BasicGetRecordsTests() );
        spy.setGetEndpoint( new URI( "http://test" ) );        
        try (MockedStatic<ClientUtils> clientUtils = Mockito.mockStatic(ClientUtils.class)) {
            clientUtils.when(() -> ClientUtils.buildGetRequest(any(URI.class), any(Map.class), any(MediaType.class)))
                    .thenReturn(rsp);
            Mockito.doReturn(entity).when(spy).getResponseEntityAsDocument(any(Response.class), nullable(String.class));
            spy.initCommonFixture(testContext);
            spy.presentTitleProperty();
        }
    }

}
