package net.officefloor.web.spi.security;

/**
 * Describes an object which the {@link HttpSecuritySource} is dependent upon.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityDependencyMetaData<D extends Enum<D>> {

	/**
	 * Obtains the {@link Enum} key identifying this dependency.
	 * 
	 * @return {@link Enum} key identifying the dependency.
	 */
	D getKey();

	/**
	 * Obtains the {@link Class} that the dependent object must
	 * extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying
	 *         the type.
	 */
	String getTypeQualifier();

	/**
	 * Provides a descriptive name for this dependency. This is useful to better
	 * describe the dependency.
	 * 
	 * @return Descriptive name for this dependency.
	 */
	String getLabel();

}