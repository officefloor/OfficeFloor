package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;

/**
 * {@link ManagedObjectExecutionStrategyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecutionStrategyTypeImpl implements ManagedObjectExecutionStrategyType {

	/**
	 * Name describing this execution strategy.
	 */
	private final String name;

	/**
	 * Instantiate.
	 * 
	 * @param name Name describing this execution strategy.
	 */
	public ManagedObjectExecutionStrategyTypeImpl(String name) {
		this.name = name;
	}

	/*
	 * ================ ManagedObjectExecutionStrategyType =====================
	 */

	@Override
	public String getExecutionStrategyName() {
		return this.name;
	}

}