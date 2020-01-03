package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;

/**
 * Implementation of the {@link ManagedObjectDependencyMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectDependencyMetaDataImpl<O extends Enum<O>> implements ManagedObjectDependencyMetaData<O> {

	/**
	 * Key identifying the dependency.
	 */
	private final O key;

	/**
	 * Type of dependency required.
	 */
	private final Class<?> type;

	/**
	 * Optional qualifier for the type.
	 */
	private String qualifier = null;

	/**
	 * Optional label to describe the dependency.
	 */
	private String label = null;

	/**
	 * Initiate.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @param type
	 *            Type of dependency.
	 */
	public ManagedObjectDependencyMetaDataImpl(O key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	/**
	 * Specifies a label to describe the dependency.
	 * 
	 * @param label
	 *            Label to describe the dependency.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	public void setTypeQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/*
	 * ================= ManagedObjectDependencyMetaData =================
	 */

	@Override
	public O getKey() {
		return this.key;
	}

	@Override
	public Class<?> getType() {
		return this.type;
	}

	@Override
	public String getTypeQualifier() {
		return this.qualifier;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}