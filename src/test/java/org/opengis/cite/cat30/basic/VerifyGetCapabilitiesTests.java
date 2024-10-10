package org.opengis.cite.cat30.basic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import jakarta.ws.rs.ProcessingException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.TestCommon;
import org.opengis.cite.cat30.util.ValidationUtils;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VerifyGetCapabilitiesTests extends TestCommon {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static Schema cswSchema;
    private static Schema atomSchema;

    @BeforeClass
    public static void initCommonFixture() throws Exception {
        testContext = mock(ITestContext.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        cswSchema = ValidationUtils.createCSWSchema();
        atomSchema = ValidationUtils.createAtomSchema();
    }

    @Test(expected = ProcessingException.class)
    public void getFullCapabilities_noService() throws SAXException,
            IOException {
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
        .thenReturn(client);
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities/basic.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName()))
                .thenReturn(cswSchema);
        when(suite.getAttribute(SuiteAttribute.ATOM_SCHEMA.getName()))
                .thenReturn(atomSchema);
        GetCapabilitiesTests iut = new GetCapabilitiesTests();
        iut.initCommonFixture(testContext);
        iut.clearMessages();
        iut.findServiceEndpoint();
        iut.getFullCapabilitiesAcceptVersion3();
    }

}
