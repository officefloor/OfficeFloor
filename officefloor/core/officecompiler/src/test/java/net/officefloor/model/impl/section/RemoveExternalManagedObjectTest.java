package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.ExternalManagedObjectModel;

/**
 * Tests removing the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveExternalManagedObjectTest extends
		AbstractSectionChangesTestCase {

	/**
	 * Initiate to use specific setup {@link SectionModel}.
	 */
	public RemoveExternalManagedObjectTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove an {@link ExternalManagedObjectModel} not in
	 * the {@link SectionModel}.
	 */
	public void testRemoveExternalManagedObjectNotInSection() {
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"NOT_IN_SECTION", null);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo,
				"Remove external managed object NOT_IN_SECTION", false,
				"External managed object NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can remove the {@link ExternalManagedObjectModel} from the
	 * {@link SectionModel} when other {@link ExternalManagedObjectModel}
	 * instances on the {@link SectionModel}.
	 */
	public void testRemoveExternalManagedObjectWhenOtherExternalManagedObjects() {
		ExternalManagedObjectModel extMo = this.model
				.getExternalManagedObjects().get(1);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo,
				"Remove external managed object OBJECT_B", true);
	}

	/**
	 * Ensure can remove the connected {@link ExternalManagedObjectModel} from
	 * the {@link SectionModel}.
	 */
	public void testRemoveExternalManagedObjectWithConnections() {
		ExternalManagedObjectModel extMo = this.model
				.getExternalManagedObjects().get(0);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo, "Remove external managed object MO",
				true);
	}

}