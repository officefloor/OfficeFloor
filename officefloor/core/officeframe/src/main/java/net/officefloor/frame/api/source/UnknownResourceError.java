package net.officefloor.frame.api.source;

/**
 * <p>
 * Indicates a resource was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the resource to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownResourceError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Location of the unknown resource.
	 */
	private final String unknownResourceLocation;

	/**
	 * Initiate.
	 * 
	 * @param unknownResourceLocation Location of the unknown resource.
	 */
	public UnknownResourceError(String unknownResourceLocation) {
		super("Can not obtain resource at location '" + unknownResourceLocation + "'");
		this.unknownResourceLocation = unknownResourceLocation;
	}

	/**
	 * Instantiate.
	 * 
	 * @param error          Triggering {@link UnknownResourceError} from
	 *                       {@link ServiceFactory}.
	 * @param serviceFactory {@link ServiceFactory}.
	 */
	public UnknownResourceError(UnknownResourceError error, ServiceFactory<?> serviceFactory) {
		super(error, serviceFactory);
		this.unknownResourceLocation = error.unknownResourceLocation;
	}

	/**
	 * Obtains the location of the unknown resource.
	 * 
	 * @return Location of the unknown resource.
	 */
	public String getUnknownResourceLocation() {
		return this.unknownResourceLocation;
	}

}