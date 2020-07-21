package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;

/**
 * Manufactures the {@link Flow} for {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowManufacturer {

	/**
	 * Creates the {@link ClassSectionFlow}.
	 * 
	 * @param context {@link ClassSectionFlowManufacturerContext}.
	 * @return {@link ClassSectionFlow} or <code>null</code> to indicate to use
	 *         another {@link ClassSectionFlowManufacturer}.
	 * @throws Exception If fails to create {@link ClassSectionFlow}.
	 */
	ClassSectionFlow createFlow(ClassSectionFlowManufacturerContext context) throws Exception;

}