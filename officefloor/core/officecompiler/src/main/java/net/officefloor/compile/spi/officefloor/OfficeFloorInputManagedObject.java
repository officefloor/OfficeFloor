package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * Input {@link ManagedObject} on the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorInputManagedObject extends OfficeFloorDependencyObjectNode {

	/**
	 * Obtains the name of this {@link OfficeFloorInputManagedObject}.
	 *
	 * @return Name of this {@link OfficeFloorInputManagedObject}.
	 */
	String getOfficeFloorInputManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this
	 * {@link OfficeFloorInputManagedObject}.
	 * <p>
	 * This enables distinguishing {@link OfficeFloorInputManagedObject}
	 * instances to enable, for example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

	/**
	 * Specifies the bound {@link OfficeFloorManagedObjectSource} for this
	 * {@link OfficeFloorInputManagedObject}.
	 *
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSource} to be bound should this
	 *            not be input but required.
	 */
	void setBoundOfficeFloorManagedObjectSource(OfficeFloorManagedObjectSource managedObjectSource);

}