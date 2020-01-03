package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * <p>
 * {@link ServiceLoader} service for the creation of a {@link ResourceSystem}.
 * <p>
 * These are loaded via the {@link ServiceLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystemFactory {

	/**
	 * <p>
	 * Obtains the protocol name for the created {@link ResourceSystem}.
	 * <p>
	 * The protocol name is used as follows <code>[protocol]:location</code> to
	 * configure a {@link ResourceSystem}.
	 * 
	 * @return Protocol name for the created {@link ResourceSystem}.
	 */
	String getProtocolName();

	/**
	 * Creates the {@link ResourceSystem}.
	 * 
	 * @param context
	 *            {@link ResourceSystemContext}.
	 * @return {@link ResourceSystem}.
	 * @throws IOException
	 *             If fails to create the {@link ResourceSystem}.
	 */
	ResourceSystem createResourceSystem(ResourceSystemContext context) throws IOException;

}