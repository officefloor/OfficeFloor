package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDependencyRequireNode;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Object} required by the {@link Office} that is to be provided by the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObject extends OfficeDependencyObjectNode, OfficeFloorDependencyRequireNode,
		DependentManagedObject, AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name that the {@link OfficeSource} refers to this {@link Object}.
	 * 
	 * @return Name that the {@link OfficeSource} refers to this {@link Object}.
	 */
	String getOfficeObjectName();

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting load this
	 * {@link ManagedObject}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done.
	 * 
	 * @param administration {@link OfficeAdministration} to be done before
	 *                       attempting load this {@link ManagedObject}.
	 */
	void addPreLoadAdministration(OfficeAdministration administration);

}