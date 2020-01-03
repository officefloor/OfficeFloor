package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a {@link ManagedObject} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectType {

	/**
	 * Obtains the name of the {@link OfficeObject} required by the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link OfficeObject} required by the {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains the fully qualified class name of the {@link Object} that must be
	 * returned from the {@link ManagedObject}.
	 * 
	 * @return Fully qualified class name of the {@link Object} that must be
	 *         returned from the {@link ManagedObject}.
	 */
	String getObjectType();

	/**
	 * <p>
	 * Obtains the qualifier on the type.
	 * <p>
	 * This is to enable qualifying the type of dependency required.
	 * 
	 * @return Qualifier on the type. May be <code>null</code> if not qualifying
	 *         the type.
	 */
	String getTypeQualifier();

	/**
	 * Obtains the fully qualified class names of the extension interfaces that
	 * must be supported by the {@link ManagedObject}.
	 * 
	 * @return Fully qualified class names of the extension interfaces that must
	 *         be supported by the {@link ManagedObject}.
	 */
	String[] getExtensionInterfaces();

}