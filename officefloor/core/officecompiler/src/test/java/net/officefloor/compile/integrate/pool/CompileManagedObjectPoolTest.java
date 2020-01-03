package net.officefloor.compile.integrate.pool;

import net.officefloor.compile.impl.structure.ManagedObjectPoolNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Ensure can compile the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileManagedObjectPoolTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectPoolSource}.
	 */
	public void testSimpleManagedObjectPoolSource() {

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if pooled object is not super type of
	 * {@link ManagedObjectSource}.
	 */
	public void testIssueIfIncorrectPooledObjectType() {

		// Record issue of incorrect type
		this.issues.recordIssue("POOL", ManagedObjectPoolNodeImpl.class,
				"Pooled object " + OtherObject.class.getName()
						+ " must be super (or same) type for ManagedObjectSource object "
						+ CompileManagedObject.class.getName());

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileManagedObject {
	}

	public static class OtherObject {
	}

}