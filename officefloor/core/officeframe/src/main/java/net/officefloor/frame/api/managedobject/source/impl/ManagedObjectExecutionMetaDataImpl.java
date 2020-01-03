package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;

/**
 * {@link ManagedObjectExecutionMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecutionMetaDataImpl implements ManagedObjectExecutionMetaData {

	/**
	 * Optional label to describe the {@link ExecutionStrategy}.
	 */
	private String label = null;

	/**
	 * Specifies the label.
	 * 
	 * @param label Label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * ================= ManagedObjectExecutionMetaData ====================
	 */

	@Override
	public String getLabel() {
		return this.label;
	}

}