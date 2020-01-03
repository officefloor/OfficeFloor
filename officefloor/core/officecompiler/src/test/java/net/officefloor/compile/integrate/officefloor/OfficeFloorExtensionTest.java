package net.officefloor.compile.integrate.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.extension.ExtendOfficeFloor;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link OfficeFloorExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExtensionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to extend the {@link OfficeFloor}.
	 */
	public void testExtendOfficeFloor() throws Exception {

		// Reset for test
		TestWork.dependency = null;

		// Compile the OfficeFloor with extension
		ExtendOfficeFloor extendOfficeFloor = new ExtendOfficeFloor();
		extendOfficeFloor.getOfficeFloorCompiler().setOfficeFloorSourceClass(TestOfficeFloorSource.class);
		OfficeFloor officeFloor = extendOfficeFloor.compileAndOpenOfficeFloor((deployer, context) -> {

			// Add the managed object
			OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.THREAD);

			// Auto-wire the object to the office
			deployer.enableAutoWireObjects();
		});

		// Ensure able to invoke the function with the object from extension
		Office office = officeFloor.getOffice("OFFICE");
		office.getFunctionManager("MAIN.function").invokeProcess(null, null);
		assertNotNull("Should have loaded object from extension", TestWork.dependency);
	}

	public static class CompileManagedObject {
	}

	public static class TestWork {

		public static CompileManagedObject dependency;

		public void function(CompileManagedObject object) {
			dependency = object;
		}
	}

	@TestSource
	public static class TestOfficeFloorSource extends AbstractOfficeFloorSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void specifyConfigurationProperties(RequiredProperties requiredProperties,
				OfficeFloorSourceContext context) throws Exception {
		}

		@Override
		public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
			deployer.addDeployedOffice("OFFICE", TestOfficeSource.class.getName(), null);
		}
	}

	@TestSource
	public static class TestOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			OfficeSection section = officeArchitect.addOfficeSection("MAIN", ClassSectionSource.class.getName(),
					TestWork.class.getName());
			OfficeSectionObject object = section.getOfficeSectionObject(CompileManagedObject.class.getName());
			OfficeObject dependency = officeArchitect.addOfficeObject("DEPENDENCY",
					CompileManagedObject.class.getName());
			officeArchitect.link(object, dependency);
		}
	}

}