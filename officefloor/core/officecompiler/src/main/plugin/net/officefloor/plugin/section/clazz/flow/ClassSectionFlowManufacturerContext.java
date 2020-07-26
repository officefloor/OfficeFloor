package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.compile.type.AnnotatedType;

/**
 * Context for the {@link ClassSectionFlowManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowManufacturerContext extends ClassSectionFlowContext {

	/**
	 * Obtains the {@link AnnotatedType} of {@link SectionFlowSourceNode}.
	 * 
	 * @return {@link AnnotatedType} of {@link SectionFlowSourceNode}.
	 */
	AnnotatedType getAnnotatedType();

}