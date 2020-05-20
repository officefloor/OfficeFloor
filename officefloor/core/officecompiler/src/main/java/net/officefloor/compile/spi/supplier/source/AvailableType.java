package net.officefloor.compile.spi.supplier.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Available type (via {@link ManagedObject}) that may source.
 * 
 * @author Daniel Sagenschneider
 */
public interface AvailableType {

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
	Class<?> getType();

}