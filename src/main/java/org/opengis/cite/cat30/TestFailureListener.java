package org.opengis.cite.cat30;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import org.opengis.cite.cat30.util.HttpMessagePart;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * A listener that augments a test result with diagnostic information in the
 * event that a test method failed. This information will appear in the XML
 * report when the test run is completed.
 */
public class TestFailureListener extends TestListenerAdapter {

    /**
     * Sets the "request" and "response" attributes of a test result. The value
     * of these attributes is a string that contains information about the
     * content of an outgoing or incoming message: target resource, status code,
     * headers, entity (if present). The entity is represented as a String with
     * UTF-8 character encoding.
     *
     * @param result A description of a test result (with a fail verdict).
     */
    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);
        Object instance = result.getInstance();
        if (CommonFixture.class.isInstance(instance)) {
            CommonFixture fixture = CommonFixture.class.cast(instance);
            result.setAttribute("request", getMessageInfo(fixture.requestInfo));
            result.setAttribute("response", getMessageInfo(fixture.responseInfo));
        }
    }

    /**
     * Summarizes the content of an HTTP message for diagnostic purposes.
     *
     * @param msgMap An EnumMap containing information gleaned from an HTTP
     * message.
     *
     * @return A String summarizing the message content.
     */
    String getMessageInfo(EnumMap<HttpMessagePart, Object> msgMap) {
        StringBuilder info = new StringBuilder();
        for (HttpMessagePart key : msgMap.keySet()) {
            info.append(key).append(":\n");
            Object value = msgMap.get(key);
            if (value.getClass().isArray()) {
                info.append(new String((byte[]) value, StandardCharsets.UTF_8));
            } else {
                info.append(value.toString());
            }
            info.append('\n');
        }
        return (info.length() > 0) ? info.toString() : "No details available.";
    }

}
