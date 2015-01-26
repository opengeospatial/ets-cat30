package org.opengis.cite.cat30.basic;

import com.sun.jersey.api.client.Client;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import com.sun.jersey.api.client.ClientHandlerException;

public class VerifyOGCServiceTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;
    private static Schema cswSchema;
    private static Client client;

    @BeforeClass
    public static void initFixture() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        cswSchema = ValidationUtils.createCSWSchema();
        client = Client.create();
    }

    @Test
    public void getFullCapabilities_noService() throws SAXException,
            IOException {
        thrown.expect(ClientHandlerException.class);
        thrown.expectMessage("Connection refused");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities-basic.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName()))
                .thenReturn(cswSchema);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        OGCServiceTests iut = new OGCServiceTests();
        iut.initOGCServiceTests(testContext);
        iut.getFullCapabilities_v3();
    }
}
