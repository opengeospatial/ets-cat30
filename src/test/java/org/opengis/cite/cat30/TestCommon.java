package org.opengis.cite.cat30;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.ISuite;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

public abstract class TestCommon {

	protected final Response rsp = mock(Response.class);

	protected final WebTarget mockWebTarget = mock(WebTarget.class);

	protected final Invocation.Builder mockBuilder = mock(Invocation.Builder.class);

	protected final Client client = mock(Client.class);

	protected static ISuite suite = mock(ISuite.class);

	protected void mockResponse() {
		when(suite.getAttribute(SuiteAttribute.CLIENT.getName())).thenReturn(client);
		when(client.target(anyString())).thenReturn(mockWebTarget);
		when(mockWebTarget.request()).thenReturn(mockBuilder);
		when(mockBuilder.get()).thenReturn(rsp);
		when(rsp.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
	}

}
