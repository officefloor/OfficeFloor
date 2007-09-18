/*
 * Created on Jan 14, 2006
 */
package net.officefloor.frame.spi.managedobject.source;

import java.io.InputStream;
import java.net.URL;

/**
 * <p>
 * Enables a
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource} to
 * access resources to configure itself.
 * </p>
 * <p>
 * This is provided by the Office Floor implemenation.
 * </p>
 * 
 * @author Daniel
 */
public interface ResourceLocator {

	/**
	 * Obtains the {@link InputStream} to the identified resource.
	 * 
	 * @param name
	 *            Name of resource to obtain the {@link InputStream}.
	 * @return {@link InputStream} to the resource. Should the resource not be
	 *         found it will return <code>null</code>.
	 */
	InputStream locateInputStream(String name);

	/**
	 * Obtains the {@link URL} to the identified resource.
	 * 
	 * @param name
	 *            Name of resource to obtain the {@link URL}.
	 * @return {@link URL} to the resource. Should the resource not be found it
	 *         will return <code>null</code>.
	 */
	URL locateURL(String name);
}