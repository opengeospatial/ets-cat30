package org.opengis.cite.cat30.basic;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

public class VerifyBasicGetRecordsTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static DocumentBuilder docBuilder;

    private static ITestContext testContext;

    private static ISuite suite;

    private static Schema cswSchema;

    private static Schema atomSchema;

    @BeforeClass
    public static void initTestFixture()
                            throws Exception {
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
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
    public void testPresentTitleProperty()
                            throws Exception {
        Document doc = docBuilder.parse( getClass().getResourceAsStream( "/capabilities/basic.xml" ) );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( doc );
        Client client = mock( Client.class );
        when( suite.getAttribute( SuiteAttribute.CLIENT.getName() ) ).thenReturn( client );
        ClientResponse rsp = mock( ClientResponse.class );
        when( client.handle( any( ClientRequest.class ) ) ).thenReturn( rsp );
        when( rsp.getStatus() ).thenReturn( ClientResponse.Status.OK.getStatusCode() );
        when( rsp.getStatus() ).thenReturn( ClientResponse.Status.OK.getStatusCode() );
        Document entity = docBuilder.parse( this.getClass().getResourceAsStream( "/getrecords/GetRecords-Summary-Response.xml" ) );

        BasicGetRecordsTests spy = Mockito.spy( new BasicGetRecordsTests() );
        spy.setGetEndpoint( new URI( "http://test" ) );
        Mockito.doReturn( entity ).when( spy ).getResponseEntityAsDocument( any( ClientResponse.class ), anyString() );
        ClientRequest req = mock( ClientRequest.class );
        Mockito.doReturn( req ).when( spy ).buildGetRequest( any( URI.class ), any( Map.class ), any( MediaType.class ) );
        spy.initCommonFixture( testContext );

        spy.presentTitleProperty();
    }

    @Test
    public void testPresentTitleProperty_invalid()
                            throws Exception {
        thrown.expect(AssertionError.class);
        Document doc = docBuilder.parse( getClass().getResourceAsStream( "/capabilities/basic.xml" ) );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( doc );
        Client client = mock( Client.class );
        when( suite.getAttribute( SuiteAttribute.CLIENT.getName() ) ).thenReturn( client );
        ClientResponse rsp = mock( ClientResponse.class );
        when( client.handle( any( ClientRequest.class ) ) ).thenReturn( rsp );
        when( rsp.getStatus() ).thenReturn( ClientResponse.Status.OK.getStatusCode() );
        when( rsp.getStatus() ).thenReturn( ClientResponse.Status.OK.getStatusCode() );
        Document entity = docBuilder.parse( this.getClass().getResourceAsStream( "/getrecords/GetRecords-Summary-Response-invalid.xml" ) );

        BasicGetRecordsTests spy = Mockito.spy( new BasicGetRecordsTests() );
        spy.setGetEndpoint( new URI( "http://test" ) );
        Mockito.doReturn( entity ).when( spy ).getResponseEntityAsDocument( any( ClientResponse.class ), anyString() );
        ClientRequest req = mock( ClientRequest.class );
        Mockito.doReturn( req ).when( spy ).buildGetRequest( any( URI.class ), any( Map.class ), any( MediaType.class ) );
        spy.initCommonFixture( testContext );

        spy.presentTitleProperty();
    }

}
