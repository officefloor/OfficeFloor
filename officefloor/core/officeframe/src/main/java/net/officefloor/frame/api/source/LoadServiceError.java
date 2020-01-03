package net.officefloor.frame.api.source;

/**
 * Indicates a service was not able to be loaded.
 * <p>
 * This is a critical error as services should always be able to be loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadServiceError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link ServiceFactory} {@link Class} name that failed to load.
	 */
	private final String serviceFactoryClassName;

	/**
	 * Initiate.
	 * 
	 * @param serviceFactoryClassName {@link ServiceFactory} {@link Class} name that
	 *                                failed to load.
	 * @param failure                 Cause.
	 */
	public LoadServiceError(String serviceFactoryClassName, Throwable failure) {
		super("Failed to create service from " + serviceFactoryClassName, failure);
		this.serviceFactoryClassName = serviceFactoryClassName;
	}

	/**
	 * Obtains the {@link ServiceFactory} {@link Class} name.
	 * 
	 * @return {@link ServiceFactory} {@link Class} name.
	 */
	public String getServiceFactoryClassName() {
		return this.serviceFactoryClassName;
	}

}