package net.officefloor.extension;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Provides convenience methods to compile an {@link OfficeFloor} with a single
 * {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtendOfficeFloor implements OfficeFloorExtensionService {

	/**
	 * {@link OfficeFloorExtensionService} logic.
	 */
	private static OfficeFloorExtensionService extender = null;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * Instantiate.
	 */
	public ExtendOfficeFloor() {
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		this.compiler.setCompilerIssues(new FailTestCompilerIssues());
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	/**
	 * Compiles the {@link OfficeFloor}.
	 * 
	 * @param officeFloorConfiguration
	 *            {@link OfficeFloorExtensionService} to configure the
	 *            {@link OfficeFloor}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile the {@link OfficeFloor}.
	 */
	public OfficeFloor compileOfficeFloor(OfficeFloorExtensionService officeFloorConfiguration) throws Exception {

		// Compile the solution
		try {
			extender = officeFloorConfiguration;

			// Compile and return the OfficeFloor
			return this.compiler.compile("OfficeFloor");

		} finally {
			// Ensure the extender is cleared for other tests
			extender = null;
		}

	}

	/**
	 * Compiles and opens the {@link Office}.
	 * 
	 * @param officeFloorConfiguration
	 *            {@link OfficeFloorExtensionService} to configure the
	 *            {@link OfficeFloor}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile and open the {@link OfficeFloor}.
	 */
	public OfficeFloor compileAndOpenOfficeFloor(OfficeFloorExtensionService officeFloorConfiguration)
			throws Exception {

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.compileOfficeFloor(officeFloorConfiguration);

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Return the OfficeFloor
		return officeFloor;
	}

	/*
	 * ====================== OfficeFloorExtensionService =====================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {
		if (extender != null) {
			extender.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

}