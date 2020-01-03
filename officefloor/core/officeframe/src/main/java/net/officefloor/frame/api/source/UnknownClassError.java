package net.officefloor.frame.api.source;

/**
 * <p>
 * Indicates a {@link Class} was not available from the {@link SourceContext}.
 * <p>
 * This is a critical error as the source is requiring the {@link Class} to
 * initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownClassError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown {@link Class}.
	 */
	private final String unknownClassName;

	/**
	 * Initiate.
	 * 
	 * @param unknownClassName Name of the unknown {@link Class}.
	 */
	public UnknownClassError(String unknownClassName) {
		super("Can not load class '" + unknownClassName + "'");
		this.unknownClassName = unknownClassName;
	}

	/**
	 * Instantiate.
	 * 
	 * @param unknownClassError Triggering {@link UnknownClassError}.
	 * @param serviceFactory    {@link ServiceFactory} requiring the property.
	 */
	public UnknownClassError(UnknownClassError unknownClassError, ServiceFactory<?> serviceFactory) {
		super(unknownClassError, serviceFactory);
		this.unknownClassName = unknownClassError.unknownClassName;
	}

	/**
	 * Obtains the name of the unknown {@link Class}.
	 * 
	 * @return Name of the unknown {@link Class}.
	 */
	public String getUnknownClassName() {
		return this.unknownClassName;
	}

}