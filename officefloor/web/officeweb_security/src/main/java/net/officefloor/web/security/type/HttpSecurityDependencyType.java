package net.officefloor.web.security.type;

import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * <code>Type definition</code> of a dependency required by the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityDependencyType<D extends Enum<D>> {

	/**
	 * Obtains the name of the dependency.
	 * 
	 * @return Name of the dependency.
	 */
	String getDependencyName();

	/**
	 * Obtains the index identifying the dependency.
	 * 
	 * @return Index identifying the dependency.
	 */
	int getIndex();

	/**
	 * Obtains the {@link Class} that the dependent object must
	 * extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getDependencyType();

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
	 * Obtains the key identifying the dependency.
	 * 
	 * @return Key identifying the dependency.
	 */
	D getKey();

}