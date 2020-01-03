package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;

/**
 * Activity undertaken for {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
public interface GovernanceActivity<F extends Enum<F>> {

	/**
	 * Undertakes the {@link Governance} activity.
	 * 
	 * @param context
	 *            {@link GovernanceContext}.
	 * @return Optional {@link FunctionState} to further execute for the
	 *         {@link GovernanceActivity}.
	 * @throws Throwable
	 *             If issue in undertaking the {@link GovernanceActivity}.
	 */
	FunctionState doActivity(GovernanceContext<F> context) throws Throwable;

}