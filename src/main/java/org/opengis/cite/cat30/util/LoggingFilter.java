package org.opengis.cite.cat30.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * <p>
 * LoggingFilter class.
 * </p>
 *
 */
public class LoggingFilter implements ClientRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class);

	/** {@inheritDoc} */
	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		Object entity = requestContext.getEntity();
		if (entity == null) {
			return;
		}
		LOG.info(entity.toString());
	}

}
