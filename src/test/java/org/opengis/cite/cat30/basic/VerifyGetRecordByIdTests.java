package org.opengis.cite.cat30.basic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.TestCommon;
import org.opengis.cite.cat30.util.ClientUtils;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class VerifyGetRecordByIdTests extends TestCommon {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static Schema cswSchema;
    private static Schema atomSchema;

    @BeforeClass
    public static void initTestFixture() throws Exception {
        testContext = mock(ITestContext.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        cswSchema = ValidationUtils.createCSWSchema();
        when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName()))
                .thenReturn(cswSchema);
        atomSchema = ValidationUtils.createAtomSchema();
        when(suite.getAttribute(SuiteAttribute.ATOM_SCHEMA.getName()))
                .thenReturn(atomSchema);
    }

    @Test
    public void getRecordByIdReturnsInvalidAtomEntry() throws SAXException,
            IOException, URISyntaxException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("schema validation error(s) detected");
        Document doc = docBuilder.parse(getClass().getResourceAsStream("/capabilities/basic.xml"));
        when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
        mockResponse();
        Document entity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom/entry-invalid.xml"));
        try (MockedStatic<ClientUtils> clientUtils = Mockito.mockStatic(ClientUtils.class);
//                MockedStatic<ServiceMetadataUtils> serviceMetadataUtils = Mockito.mockStatic(ServiceMetadataUtils.class)
                        ) {
//            serviceMetadataUtils.when(() -> ServiceMetadataUtils.getOperationEndpoint(
//                    nullable(Document.class), anyString(), anyString()))
//                    .thenReturn(new URI("http://test"));
            GetRecordByIdTests spy = Mockito.spy(new GetRecordByIdTests());
            clientUtils.when(() -> ClientUtils.buildGetRequest(nullable(URI.class), any(Map.class), any(MediaType.class)))
                    .thenReturn(rsp);
            Mockito.doReturn(entity).when(spy).getResponseEntityAsDocument(any(Response.class), nullable(String.class));
            spy.initCommonFixture(testContext);
            List<String> idList = new ArrayList<>();
            idList.add("id-01");
            spy.setIdList(idList);
            spy.getRecordByIdAsAtomEntryUsingAcceptHeader();
        }
//        Mockito.doReturn(entity).when(spy).getResponseEntityAsDocument(any(Response.class), anyString());
//        spy.initCommonFixture(testContext);
//        List<String> idList = new ArrayList<>();
//        idList.add("id-01");
//        spy.setIdList(idList);
//        spy.getRecordByIdAsAtomEntryUsingAcceptHeader();
    }

}
