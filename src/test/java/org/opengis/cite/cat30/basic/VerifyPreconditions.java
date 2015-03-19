package org.opengis.cite.cat30.basic;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opengis.cite.cat30.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VerifyPreconditions {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;

    @BeforeClass
    public static void initCommonFixture() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void verifyTestSubjectIsNotCapabilitiesDoc() throws SAXException,
            IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Document element in unexpected namespace");
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/atom/feed.xml"));
        when(suite.getAttribute(SUBJ)).thenReturn(doc);
        SuitePreconditions iut = new SuitePreconditions();
        iut.verifyTestSubject(testContext);
    }

}
