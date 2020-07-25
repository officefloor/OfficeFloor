package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectContext;

/**
 * Context for the {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionLoaderContext {

	/**
	 * Obtains the {@link ClassSectionObjectContext}.
	 * 
	 * @return {@link ClassSectionObjectContext}.
	 */
	ClassSectionObjectContext getSectionObjectContext();

	/**
	 * Obtains the {@link ClassSectionFlowContext}.
	 * 
	 * @return {@link ClassSectionFlowContext}.
	 */
	ClassSectionFlowContext getSectionFlowContext();

	/**
	 * Obtains the {@link SectionDesigner}.
	 * 
	 * @return {@link SectionDesigner}.
	 */
	SectionDesigner getSectionDesigner();

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSectionSourceContext();

}