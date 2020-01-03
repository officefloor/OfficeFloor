package net.officefloor.compile.executive;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * <code>Type definition</code> of an {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveType {

	/**
	 * Obtains the {@link ExecutionStrategyType} definitions for the
	 * {@link ExecutionStrategy} instances available from the {@link Executive}.
	 * 
	 * @return {@link ExecutionStrategyType} definitions for the
	 *         {@link ExecutionStrategy} instances available from the
	 *         {@link Executive}.
	 */
	ExecutionStrategyType[] getExecutionStrategyTypes();

	/**
	 * Obtains the {@link TeamOversightType} definitions for the
	 * {@link TeamOversight} instances available from the {@link Executive}.
	 * 
	 * @return {@link TeamOversightType} definitions for the {@link TeamOversight}
	 *         instances available from the {@link Executive}.
	 */
	TeamOversightType[] getTeamOversightTypes();
}