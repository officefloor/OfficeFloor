package net.officefloor.web.resource;

/**
 * HTTP resource.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResource {

	/**
	 * <p>
	 * Obtains the path to this {@link HttpResource}.
	 * <p>
	 * The path is canonical to allow using it as a key for caching this
	 * {@link HttpResource}.
	 * 
	 * @return Canonical path to this {@link HttpResource}.
	 */
	String getPath();

	/**
	 * <p>
	 * Indicates if this {@link HttpResource} exists. Should this
	 * {@link HttpResource} not exist, only the path will be available.
	 * <p>
	 * This allows for caching of {@link HttpResource} instances not existing.
	 * 
	 * @return <code>true</code> if this {@link HttpResource} exists.
	 */
	boolean isExist();

}