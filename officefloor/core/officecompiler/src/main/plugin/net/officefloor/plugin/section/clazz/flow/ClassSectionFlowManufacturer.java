package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.section.clazz.AbstractClassSectionSource;

/**
 * Manufactures the {@link Flow} for {@link AbstractClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowManufacturer {

	/**
	 * Creates the {@link SectionFlowSinkNode}.
	 * 
	 * @param context {@link ClassSectionFlowManufacturerContext}.
	 * @return {@link SectionFlowSinkNode} or <code>null</code> to indicate to use
	 *         another {@link ClassSectionFlowManufacturer}.
	 * @throws Exception If fails to create {@link SectionFlowSinkNode}.
	 */
	SectionFlowSinkNode createFlowSink(ClassSectionFlowManufacturerContext context) throws Exception;

}