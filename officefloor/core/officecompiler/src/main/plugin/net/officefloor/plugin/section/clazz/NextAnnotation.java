package net.officefloor.plugin.section.clazz;

/**
 * {@link Next} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class NextAnnotation {

	/**
	 * Name of the {@link Next}.
	 */
	private final String nextName;

	/**
	 * Argument type for the {@link Next}.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param nextName     Name of the {@link Next}.
	 * @param argumentType Argument type for the {@link Next}.
	 */
	public NextAnnotation(String nextName, Class<?> argumentType) {
		this.nextName = nextName;
		this.argumentType = argumentType;
	}

	/**
	 * Instantiate.
	 * 
	 * @param next         {@link Next}.
	 * @param argumentType Argument type for the {@link Next}.
	 */
	public NextAnnotation(Next next, Class<?> argumentType) {
		this(next.value(), argumentType);
	}

	/**
	 * Instantiate.
	 * 
	 * @param nextFunction {@link NextFunction}.
	 * @param argumentType Argument type for the {@link NextFunction}.
	 */
	@SuppressWarnings("deprecation")
	public NextAnnotation(NextFunction nextFunction, Class<?> argumentType) {
		this(nextFunction.value(), argumentType);
	}

	/**
	 * Obtains the {@link Next} name.
	 * 
	 * @return {@link Next} name.
	 */
	public String getNextName() {
		return this.nextName;
	}

	/**
	 * Obtains the argument type for the {@link Next}.
	 * 
	 * @return Argument type for the {@link Next}.
	 */
	public Class<?> getArgumentType() {
		return this.argumentType;
	}
}