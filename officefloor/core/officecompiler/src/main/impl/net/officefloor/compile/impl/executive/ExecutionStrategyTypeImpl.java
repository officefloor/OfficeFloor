package net.officefloor.compile.impl.executive;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.frame.api.executive.ExecutionStrategy;

/**
 * {@link ExecutionStrategyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionStrategyTypeImpl implements ExecutionStrategyType {

	/**
	 * {@link ExecutionStrategy} name.
	 */
	private final String executionStrategyName;

	/**
	 * Instantiate.
	 * 
	 * @param executionStrategyName {@link ExecutionStrategy} name.
	 */
	public ExecutionStrategyTypeImpl(String executionStrategyName) {
		this.executionStrategyName = executionStrategyName;
	}

	/*
	 * ============== ExecutionStrategyType =================
	 */

	@Override
	public String getExecutionStrategyName() {
		return this.executionStrategyName;
	}

}