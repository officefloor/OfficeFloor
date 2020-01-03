package net.officefloor.compile.impl.object;

import net.officefloor.compile.internal.structure.DependentObjectNode;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.TypeQualification;

/**
 * {@link DependentObjectType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class DependentObjectTypeImpl implements DependentObjectType {

	/**
	 * Name of the {@link DependentObjectNode}.
	 */
	private final String objectName;

	/**
	 * {@link TypeQualification} instances for the {@link DependentObjectNode}.
	 */
	private TypeQualification[] typeQualifications;

	/**
	 * {@link ObjectDependencyType} instances for the
	 * {@link DependentObjectNode}.
	 */
	private ObjectDependencyType[] dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link DependentObjectNode}.
	 * @param typeQualifications
	 *            {@link TypeQualification} instances for the
	 *            {@link DependentObjectNode}.
	 * @param dependencies
	 *            {@link ObjectDependencyType} instances for the
	 *            {@link DependentObjectNode}.
	 */
	public DependentObjectTypeImpl(String objectName,
			TypeQualification[] typeQualifications,
			ObjectDependencyType[] dependencies) {
		this.objectName = objectName;
		this.typeQualifications = typeQualifications;
		this.dependencies = dependencies;
	}

	/*
	 * ==================== DependentObjectType ============================
	 */

	@Override
	public String getDependentObjectName() {
		return this.objectName;
	}

	@Override
	public TypeQualification[] getTypeQualifications() {
		return this.typeQualifications;
	}

	@Override
	public ObjectDependencyType[] getObjectDependencies() {
		return this.dependencies;
	}

}