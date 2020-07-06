package net.officefloor.plugin.section.clazz;

import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;

/**
 * Context for the {@link AbstractClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowContext {

	/**
	 * Adds a {@link SectionFunctionNamespace}.
	 * 
	 * @param managedFunctionSourceClassName {@link ManagedFunctionSource}
	 *                                       {@link Class} name.
	 * @param properties                     {@link PropertyList} for the
	 *                                       {@link SectionFunctionNamespace}.
	 */
	void addFunctionNamespace(String managedFunctionSourceClassName, PropertyList properties);

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @param functionName Name of the {@link SectionFunction}.
	 * @return {@link SectionFunction}.
	 */
	SectionFunction getFunction(String functionName);

	/**
	 * Gets or creates the {@link SubSection}.
	 * 
	 * @param sectionSourceClassName Name of {@link SectionSource} {@link Class}.
	 * @param sectionLocation        Location of the {@link SubSection}.
	 * @param properties             {@link PropertyList} for the
	 *                               {@link SubSection}.
	 * @param outputMappings         Mapping of {@link SubSectionOutput} name to
	 *                               name of handling {@link SectionFlowSinkNode}.
	 * @return {@link SubSection}.
	 */
	SubSection getOrCreateSubSection(String sectionSourceClassName, String sectionLocation, PropertyList properties,
			Map<String, String> outputMappings);

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getContext();

}