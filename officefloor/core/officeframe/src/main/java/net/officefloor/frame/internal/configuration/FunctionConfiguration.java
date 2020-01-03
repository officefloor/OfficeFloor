package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;

/**
 * Configuration of a {@link ManagedFunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionConfiguration<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Team} to execute the
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @return Name of {@link Team}. May be <code>null</code> to use any
	 *         {@link Team}.
	 */
	String getResponsibleTeamName();

	/**
	 * Obtains the configuration of the {@link Flow} instances for this
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @return Configuration of {@link Flow} instances for this
	 *         {@link ManagedFunctionLogic}.
	 */
	FlowConfiguration<F>[] getFlowConfiguration();

	/**
	 * Obtains the {@link EscalationConfiguration} instances.
	 * 
	 * @return {@link EscalationConfiguration} instances.
	 */
	EscalationConfiguration[] getEscalations();

}