package net.officefloor.activity;

import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.configuration.ConfigurationItem;

/**
 * Context for the {@link ActivityLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link ProcedureArchitect}.
	 * 
	 * @return {@link ProcedureArchitect}.
	 */
	ProcedureArchitect<SubSection> getProcedureArchitect();

	/**
	 * Obtains the {@link ProcedureLoader}.
	 * 
	 * @return {@link ProcedureLoader}.
	 */
	ProcedureLoader getProcedureLoader();

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