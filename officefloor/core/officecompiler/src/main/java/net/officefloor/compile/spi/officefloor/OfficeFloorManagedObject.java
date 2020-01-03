package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * {@link ManagedObject} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObject extends OfficeFloorDependencyObjectNode {

	/**
	 * Obtains the name of this {@link OfficeFloorManagedObject}.
	 * 
	 * @return Name of this {@link OfficeFloorManagedObject}.
	 */
	String getOfficeFloorManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this
	 * {@link OfficeFloorManagedObject}.
	 * <p>
	 * This enables distinguishing {@link OfficeFloorManagedObject} instances to
	 * enable, for example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @return {@link OfficeFloorManagedObjectDependency}.
	 */
	OfficeFloorManagedObjectDependency getOfficeFloorManagedObjectDependency(String managedObjectDependencyName);

}