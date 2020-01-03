package net.officefloor.compile.section;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Provides type qualification for a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeQualification {

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier. May be <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the type.
	 * 
	 * @return Type.
	 */
	String getType();

}