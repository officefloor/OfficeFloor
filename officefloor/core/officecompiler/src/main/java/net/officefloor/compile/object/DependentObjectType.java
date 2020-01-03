package net.officefloor.compile.object;

import net.officefloor.compile.section.TypeQualification;

/**
 * <code>Type definition</code> for a dependent object.
 *
 * @author Daniel Sagenschneider
 */
public interface DependentObjectType {

	/**
	 * Obtains the name of this dependent object.
	 * 
	 * @return Name of this dependent object.
	 */
	String getDependentObjectName();

	/**
	 * <p>
	 * Obtains the {@link TypeQualification} instances for this
	 * {@link DependentObjectType}.
	 * <p>
	 * Should no {@link TypeQualification} instances be manually assigned, the
	 * {@link TypeQualification} should be derived from the object type (i.e.
	 * type without qualifier).
	 * 
	 * @return {@link TypeQualification} instances for this dependent object.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * <p>
	 * Obtains the {@link ObjectDependencyType} instances for this dependent
	 * object.
	 * <p>
	 * This allows determining transitive dependencies.
	 * 
	 * @return {@link ObjectDependencyType} instances for this dependent object.
	 */
	ObjectDependencyType[] getObjectDependencies();

}