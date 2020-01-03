package net.officefloor.server.stream;

import java.io.InputStream;

/**
 * Provides non-blocking {@link InputStream} for servicing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ServerInputStream extends InputStream {

	/**
	 * Obtains a new {@link InputStream} that starts browsing the input content
	 * from the current position of the {@link ServerInputStream} within the
	 * input stream of data.
	 * 
	 * @return {@link InputStream}.
	 */
	public abstract InputStream createBrowseInputStream();

}