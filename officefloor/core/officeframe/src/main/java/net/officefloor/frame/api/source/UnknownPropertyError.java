package net.officefloor.frame.api.source;

/**
 * <p>
 * Indicates a property was not configured within the {@link SourceProperties}.
 * <p>
 * This is a critical error as the source is requiring this property to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownPropertyError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown property.
	 */
	private final String unknownPropertyName;

	/**
	 * Initiate.
	 * 
	 * @param unknownPropertyName Name of the unknown property.
	 */
	public UnknownPropertyError(String unknownPropertyName) {
		super("Must specify property '" + unknownPropertyName + "'");
		this.unknownPropertyName = unknownPropertyName;
	}

	/**
	 * Instantiate.
	 * 
	 * @param unknownPropertyError Triggering {@link UnknownPropertyError}.
	 * @param serviceFactory       {@link ServiceFactory} requiring the property.
	 */
	public UnknownPropertyError(UnknownPropertyError unknownPropertyError, ServiceFactory<?> serviceFactory) {
		super(unknownPropertyError, serviceFactory);
		this.unknownPropertyName = unknownPropertyError.unknownPropertyName;
	}

	/**
	 * Obtains the name of the unknown property.
	 * 
	 * @return Name of the unknown property.
	 */
	public String getUnknownPropertyName() {
		return this.unknownPropertyName;
	}

}