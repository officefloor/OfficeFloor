package net.officefloor.web.security.type;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * <code>Type definition</code> of a {@link Flow} instigated by the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Flow}.
	 * 
	 * @return Name of the {@link Flow}.
	 */
	String getFlowName();

	/**
	 * Obtains the key identifying the {@link Flow}.
	 * 
	 * @return Key identifying the {@link Flow}.
	 */
	F getKey();

	/**
	 * Obtains the index identifying the {@link Flow}.
	 * 
	 * @return Index identifying the {@link Flow}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed to the {@link Flow}.
	 * 
	 * @return Type of argument passed to the {@link Flow}. May be
	 *         <code>null</code> to indicate no argument.
	 */
	Class<?> getArgumentType();

}