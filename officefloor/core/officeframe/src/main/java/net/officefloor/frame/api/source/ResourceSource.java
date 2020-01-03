package net.officefloor.frame.api.source;

import java.io.InputStream;

/**
 * Source for resources.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSource {

	/**
	 * Attempts to source the resource.
	 * 
	 * @param location
	 *            Location of the resource.
	 * @return {@link InputStream} to the content of the resource or
	 *         <code>null</code> if not able to source the resource.
	 */
	InputStream sourceResource(String location);

}