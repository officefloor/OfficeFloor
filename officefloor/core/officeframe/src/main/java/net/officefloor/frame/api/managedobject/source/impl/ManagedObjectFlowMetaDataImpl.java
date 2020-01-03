package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedObjectFlowMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFlowMetaDataImpl<F extends Enum<F>> implements
		ManagedObjectFlowMetaData<F> {

	/**
	 * Key identifying the {@link Flow}.
	 */
	private final F key;

	/**
	 * Type of argument passed to the {@link Flow}.
	 */
	private final Class<?> argumentType;

	/**
	 * Optional label to describe the {@link Flow}.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the {@link Flow}.
	 */
	public ManagedObjectFlowMetaDataImpl(F key, Class<?> argumentType) {
		this.key = key;
		this.argumentType = argumentType;
	}

	/**
	 * Specifies a label to describe the {@link Flow}.
	 * 
	 * @param label
	 *            Label to describe the {@link Flow}.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * ==================== ManagedObjectFlowMetaData ======================
	 */

	@Override
	public F getKey() {
		return this.key;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}