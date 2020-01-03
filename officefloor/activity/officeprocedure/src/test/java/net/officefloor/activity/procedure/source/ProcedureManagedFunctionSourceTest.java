package net.officefloor.activity.procedure.source;

import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProcedureManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure specification correct.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(ProcedureManagedFunctionSource.class,
				ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME, "Class",
				ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME, "Source",
				ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME, "Procedure");
	}

}