package net.officefloor.compile.governance;

import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link Flow} possibly instigated by
 * a {@link GovernanceActivity}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceFlowType<F extends Enum<F>> {

	/**
	 * Obtains the name for the {@link GovernanceFlowType}.
	 * 
	 * @return Name for the {@link GovernanceFlowType}.
	 */
	String getFlowName();

	/**
	 * <p>
	 * Obtains the index for the {@link GovernanceFlowType}.
	 * <p>
	 * Should there be an {@link Enum} then will be the {@link Enum#ordinal()}
	 * value. Otherwise will be the index that this was added.
	 * 
	 * @return Index for the {@link GovernanceFlowType}.
	 */
	int getIndex();

	/**
	 * Obtains the type of the argument passed by the {@link GovernanceActivity}
	 * to the {@link Flow}.
	 * 
	 * @return Type of argument passed to {@link Flow}. May be
	 *         <code>null</code> to indicate no argument.
	 */
	Class<?> getArgumentType();

	/**
	 * Obtains the {@link Enum} key for the {@link GovernanceFlowType}.
	 * 
	 * @return {@link Enum} key for the {@link GovernanceFlowType}. May be
	 *         <code>null</code> if no {@link Enum} for flows.
	 */
	F getKey();

}