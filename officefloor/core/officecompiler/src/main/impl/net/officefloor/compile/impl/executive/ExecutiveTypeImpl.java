package net.officefloor.compile.impl.executive;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.executive.TeamOversightType;

/**
 * {@link ExecutiveType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveTypeImpl implements ExecutiveType {

	/**
	 * {@link ExecutionStrategyType} instances.
	 */
	private final ExecutionStrategyType[] executionStrategyTypes;

	/**
	 * {@link TeamOversightType} instances.
	 */
	private final TeamOversightType[] teamOversightTypes;

	/**
	 * Instantiate.
	 * 
	 * @param executionStrategyTypes {@link ExecutionStrategyType} instances.
	 * @param teamOversightTypes     {@link TeamOversightType} instances.
	 */
	public ExecutiveTypeImpl(ExecutionStrategyType[] executionStrategyTypes, TeamOversightType[] teamOversightTypes) {
		this.executionStrategyTypes = executionStrategyTypes;
		this.teamOversightTypes = teamOversightTypes;
	}

	/*
	 * ================ ExecutiveType =====================
	 */

	@Override
	public ExecutionStrategyType[] getExecutionStrategyTypes() {
		return this.executionStrategyTypes;
	}

	@Override
	public TeamOversightType[] getTeamOversightTypes() {
		return this.teamOversightTypes;
	}

}