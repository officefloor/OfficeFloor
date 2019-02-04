package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;

/**
 * {@link ManagedObjectFunctionDependencyType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFunctionDependencyTypeImpl implements ManagedObjectFunctionDependencyType {

	/**
	 * {@link ManagedObjectFunctionDependency} name.
	 */
	private final String name;

	/**
	 * {@link ManagedObjectFunctionDependency} type.
	 */
	private final Class<?> type;

	/**
	 * Instantiate.
	 * 
	 * @param name {@link ManagedObjectFunctionDependency} name.
	 * @param type {@link ManagedObjectFunctionDependency} type.
	 */
	public ManagedObjectFunctionDependencyTypeImpl(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	/*
	 * ================= ManagedObjectFunctionDependencyType =========
	 */

	@Override
	public String getFunctionObjectName() {
		return this.name;
	}

	@Override
	public Class<?> getFunctionObjectType() {
		return this.type;
	}

}