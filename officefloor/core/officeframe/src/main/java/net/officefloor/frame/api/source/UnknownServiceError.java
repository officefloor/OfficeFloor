package net.officefloor.frame.api.source;

/**
 * <p>
 * Indicates a service was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the service to initialise
 * and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownServiceError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link ServiceFactory} type that is not configured.
	 */
	private final Class<?> unknownServiceFactoryType;

	/**
	 * Initiate.
	 * 
	 * @param unknownServiceFactoryType {@link ServiceFactory} type that is not
	 *                                  configured.
	 */
	public UnknownServiceError(Class<?> unknownServiceFactoryType) {
		super("No services configured for " + unknownServiceFactoryType.getName());
		this.unknownServiceFactoryType = unknownServiceFactoryType;
	}

	/**
	 * Obtains the {@link ServiceFactory} type that is not configured.
	 * 
	 * @return {@link ServiceFactory} type that is not configured.
	 */
	public Class<?> getUnknownServiceFactoryType() {
		return this.unknownServiceFactoryType;
	}

}