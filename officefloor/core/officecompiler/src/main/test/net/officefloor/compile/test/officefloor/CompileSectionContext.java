package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Context for the {@link CompileSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileSectionContext {

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