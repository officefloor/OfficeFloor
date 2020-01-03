package net.officefloor.activity.procedure.source;

import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.section.ProcedureSectionSource;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProcedureSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(ProcedureSectionSource.class,
				ProcedureManagedFunctionSource.RESOURCE_PROPERTY_NAME, "Class",
				ProcedureManagedFunctionSource.SOURCE_NAME_PROPERTY_NAME, "Source");
	}

}