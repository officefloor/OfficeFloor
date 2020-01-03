package net.officefloor.server.ssl;

import javax.net.ssl.SSLContext;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Source for {@link SSLContext} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SslContextSource {

	/**
	 * Creates a new {@link SSLContext}.
	 * 
	 * @param context
	 *            {@link SourceContext} to configure the {@link SSLContext}.
	 * @return New {@link SSLContext} ready for use.
	 * @throws Exception
	 *             If fails to create the {@link SSLContext} (possibly because a
	 *             protocol or cipher is not supported).
	 */
	SSLContext createSslContext(SourceContext context) throws Exception;

}