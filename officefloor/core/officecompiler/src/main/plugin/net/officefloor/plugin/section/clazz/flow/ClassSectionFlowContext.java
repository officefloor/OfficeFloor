package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} context for {@link Class} section.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowContext {

	/**
	 * Adds a {@link SectionFunctionNamespace}.
	 * 
	 * @param namespaceName                  Hit to use as name of
	 *                                       {@link SectionFunctionNamespace}. May
	 *                                       alter to keep name unique.
	 * @param managedFunctionSourceClassName {@link ManagedFunctionSource}
	 *                                       {@link Class} name.
	 * @param properties                     {@link PropertyList} for the
	 *                                       {@link SectionFunctionNamespace}.
	 */
	void addFunctionNamespace(String namespaceName, String managedFunctionSourceClassName, PropertyList properties);

	/**
	 * Obtains the {@link SectionFunction}.
	 * 
	 * @param functionName Name of the {@link SectionFunction}.
	 * @return {@link SectionFunction} or <code>null</code> if no
	 *         {@link SectionFunction} by name.
	 */
	SectionFunction getFunction(String functionName);

	/**
	 * Creates the {@link ClassSectionSubSectionOutputLink}.
	 * 
	 * @param subSectionOutputName Name of {@link SubSectionOutput}.
	 * @param linkName             Name of handling {@link SectionFlowSinkNode}.
	 * @return {@link ClassSectionSubSectionOutputLink}.
	 */
	ClassSectionSubSectionOutputLink createSubSectionOutputLink(String subSectionOutputName, String linkName);

	/**
	 * Gets or creates the {@link SubSection}.
	 * 
	 * @param sectionName            Hint to use as name of {@link SubSection}. May
	 *                               alter to keep name unique.
	 * @param sectionSourceClassName Name of {@link SectionSource} {@link Class}.
	 * @param sectionLocation        Location of the {@link SubSection}.
	 * @param properties             {@link PropertyList} for the
	 *                               {@link SubSection}.
	 * @param configuredLinks        {@link ClassSectionSubSectionOutputLink}
	 *                               instances.
	 * @return {@link SubSection}.
	 */
	SubSection getOrCreateSubSection(String sectionName, String sectionSourceClassName, String sectionLocation,
			PropertyList properties, ClassSectionSubSectionOutputLink... configuredLinks);

	/**
	 * Obtains the {@link SectionFlowSinkNode}.
	 * 
	 * @param flowName     Name of {@link Flow}.
	 * @param argumentType Fully qualified type of argument. May be
	 *                     <code>null</code> for no argument.
	 * @return {@link SectionFlowSinkNode}.
	 */
	SectionFlowSinkNode getFlowSink(String flowName, String argumentType);

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}