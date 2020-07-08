package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link ClassSectionFlowManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionFlowManufacturerContext {

	/**
	 * Obtains the {@link AnnotatedType} of {@link SectionFlowSourceNode}.
	 * 
	 * @return {@link AnnotatedType} of {@link SectionFlowSourceNode}.
	 */
	AnnotatedType getAnnotatedType();

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
	 * @param sectionSourceClassName Name of {@link SectionSource} {@link Class}.
	 * @param sectionLocation        Location of the {@link SubSection}.
	 * @param properties             {@link PropertyList} for the
	 *                               {@link SubSection}.
	 * @param configuredLinks        {@link ClassSectionSubSectionOutputLink}
	 *                               instances.
	 * @return {@link SubSection}.
	 */
	SubSection getOrCreateSubSection(String sectionSourceClassName, String sectionLocation, PropertyList properties,
			ClassSectionSubSectionOutputLink... configuredLinks);

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