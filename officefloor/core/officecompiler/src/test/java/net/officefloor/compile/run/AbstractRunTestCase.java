package net.officefloor.compile.run;

import net.officefloor.compile.AbstractModelCompilerTestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.integrate.managedfunction.CompileFunctionTest.InputManagedObject;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.model.impl.officefloor.OfficeFloorModelOfficeFloorSource;

/**
 * Ensure able to run with an {@link InputManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRunTestCase extends AbstractModelCompilerTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	@Override
	protected void tearDown() throws Exception {

		// Ensure close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Compiles and opens the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	protected OfficeFloor open() {

		// Ensure open
		assertNull("OfficeFloor already compiled", this.officeFloor);

		// Obtain the resource source
		ResourceSource resourceSource = this.getResourceSource();

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setOfficeFloorSourceClass(OfficeFloorModelOfficeFloorSource.class);
		compiler.setOfficeFloorLocation("office-floor");
		compiler.addResources(resourceSource);

		// Compile the OfficeFloor
		this.officeFloor = compiler.compile("OfficeFloor");
		assertNotNull("Should compile the OfficeFloor", officeFloor);

		// Open the OfficeFloor
		try {
			this.officeFloor.openOfficeFloor();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Return the open OfficeFloor
		return this.officeFloor;
	}

}