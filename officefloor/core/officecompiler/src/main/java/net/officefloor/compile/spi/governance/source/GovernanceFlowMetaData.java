package net.officefloor.compile.spi.governance.source;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Describes a {@link Flow} required by the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceFlowMetaData<F extends Enum<F>> {

	/**
	 * Obtains the {@link Enum} key identifying this {@link Flow}. If
	 * <code>null</code> then {@link Flow} will be referenced by this instance's
	 * index in the array returned from {@link GovernanceSourceMetaData}.
	 * 
	 * @return {@link Enum} key identifying the {@link Flow} or
	 *         <code>null</code> indicating identified by an index.
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
	 * Provides a descriptive name for this {@link Flow}. This is useful to
	 * better describe the {@link Flow}.
	 * 
	 * @return Descriptive name for this {@link Flow}.
	 */
	String getLabel();

}