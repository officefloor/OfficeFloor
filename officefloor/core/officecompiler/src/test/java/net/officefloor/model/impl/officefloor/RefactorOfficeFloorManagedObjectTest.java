package net.officefloor.model.impl.officefloor;

import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.TypeQualificationModel;

/**
 * Refactors the {@link OfficeFloorManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeFloorManagedObjectTest extends AbstractOfficeFloorChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeFloorManagedObjectModel officeMo = this.model.getOfficeFloorManagedObjects().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeFloorManagedObjectTypeQualification(officeMo,
				"QUALIFIER", "TYPE");
		this.assertChange(change, change.getTarget(), "Add Managed Object Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeFloorManagedObjectModel officeMo = this.model.getOfficeFloorManagedObjects().get(0);
		TypeQualificationModel typeQualification = officeMo.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations
				.removeOfficeFloorManagedObjectTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Managed Object Type Qualification", true);
	}

}