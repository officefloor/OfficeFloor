package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureObjectType;

/**
 * {@link ProcedureObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureObjectTypeImpl implements ProcedureObjectType {

	/**
	 * Name of {@link Object}.
	 */
	private final String objectName;

	/**
	 * {@link Object} type.
	 */
	private final Class<?> objectType;

	/**
	 * Qualifier for the {@link Object}.
	 */
	private final String typeQualifier;

	/**
	 * Instantiate.
	 * 
	 * @param objectName    Name of {@link Object}.
	 * @param objectType    {@link Object} type.
	 * @param typeQualifier Qualifier for the {@link Object}.
	 */
	public ProcedureObjectTypeImpl(String objectName, Class<?> objectType, String typeQualifier) {
		this.objectName = objectName;
		this.objectType = objectType;
		this.typeQualifier = typeQualifier;
	}

	/*
	 * ================ ProcedureObjectType ====================
	 */

	@Override
	public String getObjectName() {
		return this.objectName;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

}