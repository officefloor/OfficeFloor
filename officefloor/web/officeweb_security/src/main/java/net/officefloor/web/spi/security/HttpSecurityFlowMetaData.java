package net.officefloor.web.spi.security;

import net.officefloor.frame.internal.structure.Flow;

/**
 * Describes a {@link Flow} required by the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityFlowMetaData<F extends Enum<F>> {

	/**
	 * Obtains the {@link Enum} key identifying the application
	 * {@link Flow} to instigate.
	 * 
	 * @return {@link Enum} key identifying the application {@link Flow}
	 *         to instigate.
	 */
	F getKey();

	/**
	 * <p>
	 * Obtains the {@link Class} of the argument that is passed to the
	 * {@link Flow}.
	 * <p>
	 * This may be <code>null</code> to indicate no argument is passed.
	 * 
	 * @return Type of the argument that is passed to the {@link Flow}.
	 */
	Class<?> getArgumentType();

	/**
	 * Provides a descriptive name for this {@link Flow}. This is useful
	 * to better describe the {@link Flow}.
	 * 
	 * @return Descriptive name for this {@link Flow}.
	 */
	String getLabel();

}