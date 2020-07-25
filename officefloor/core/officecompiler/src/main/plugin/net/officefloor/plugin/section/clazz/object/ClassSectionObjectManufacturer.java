package net.officefloor.plugin.section.clazz.object;

import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Manufactures the {@link Object} for {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionObjectManufacturer {

	/**
	 * Creates the {@link SectionDependencyObjectNode}.
	 * 
	 * @param context {@link ClassSectionObjectManufacturerContext}.
	 * @return {@link SectionDependencyObjectNode} or <code>null</code> to indicate
	 *         to use another {@link ClassSectionObjectManufacturer}.
	 * @throws Exception If fails to create {@link SectionDependencyObjectNode}.
	 */
	SectionDependencyObjectNode createObject(ClassSectionObjectManufacturerContext context) throws Exception;

}