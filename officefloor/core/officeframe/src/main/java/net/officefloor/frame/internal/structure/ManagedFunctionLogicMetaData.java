package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;

/**
 * Meta-data for a {@link ManagedFunctionLogic} to be executed within a
 * {@link ManagedFunctionContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link ManagedFunctionLogic}.
	 * 
	 * @return Name of the {@link ManagedFunctionLogic}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link TeamManagement} responsible for completion of the
	 * {@link FunctionState}.
	 * 
	 * @return {@link TeamManagement} responsible for completion of the
	 *         {@link FunctionState}. May be <code>null</code> to enable any
	 *         {@link Team} to execute the {@link FunctionState}.
	 */
	TeamManagement getResponsibleTeam();

	/**
	 * Obtains the time out for {@link AsynchronousFlow} instigated by the
	 * {@link ManagedFunction}.
	 * 
	 * @return Time out for {@link AsynchronousFlow} instigated by the
	 *         {@link ManagedFunction}.
	 */
	long getAsynchronousFlowTimeout();

	/**
	 * Obtains the {@link AssetManager} that manages {@link AsynchronousFlow}
	 * instances instigated by the {@link ManagedFunction}.
	 * 
	 * @return {@link AssetManager} that manages {@link AsynchronousFlow} instances
	 *         instigated by the {@link ManagedFunction}.
	 */
	AssetManager getAsynchronousFlowManager();

	/**
	 * Obtains the {@link FlowMetaData} of the specified {@link Flow}.
	 * 
	 * @param flowIndex Index of the {@link Flow}.
	 * @return {@link FlowMetaData} of the specified {@link Flow}.
	 */
	FlowMetaData getFlow(int flowIndex);

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the next
	 * {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of next {@link ManagedFunction}.
	 */
	ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link ManagedFunctionLogic}.
	 * 
	 * @return {@link EscalationProcedure}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * Obtains the {@link OfficeMetaData}.
	 * 
	 * @return {@link OfficeMetaData}.
	 */
	OfficeMetaData getOfficeMetaData();

}