package net.officefloor.plugin.section.clazz.flow.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.PropertyValue;
import net.officefloor.plugin.section.clazz.SectionInterface;
import net.officefloor.plugin.section.clazz.SectionNameAnnotation;
import net.officefloor.plugin.section.clazz.SectionOutputLink;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.flow.ClassSectionSubSectionOutputLink;

/**
 * {@link ClassSectionFlowManufacturer} for {@link SubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionClassSectionFlowManufacturer
		implements ClassSectionFlowManufacturer, ClassSectionFlowManufacturerServiceFactory {

	/*
	 * ================ ClassSectionFlowManufacturerServiceFactory ================
	 */

	@Override
	public ClassSectionFlowManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ClassSectionFlowManufacturer ========================
	 */

	@Override
	public SectionFlowSinkNode createFlowSink(ClassSectionFlowManufacturerContext context) throws Exception {

		// Determine if section interface
		AnnotatedType annotatedType = context.getAnnotatedType();
		SectionInterface sectionInterface = annotatedType.getAnnotation(SectionInterface.class);
		if (sectionInterface == null) {
			return null; // not section interface
		}

		// Obtain the section name
		SectionNameAnnotation nameAnnotation = annotatedType.getAnnotation(SectionNameAnnotation.class);
		String sectionName = nameAnnotation != null ? nameAnnotation.getName() : "SECTION";

		// Build the section
		String location = getValue(sectionInterface.locationClass(), sectionInterface.location());
		PropertyList properties = context.getSourceContext().createPropertyList();
		for (PropertyValue property : sectionInterface.properties()) {
			String value = getValue(property.valueClass(), property.value());
			properties.addProperty(property.name()).setValue(value);
		}
		List<ClassSectionSubSectionOutputLink> links = new LinkedList<>();
		for (SectionOutputLink outputLink : sectionInterface.outputs()) {
			ClassSectionSubSectionOutputLink link = context.createSubSectionOutputLink(outputLink.name(),
					outputLink.link());
			links.add(link);
		}
		SubSection subSection = context.getOrCreateSubSection(sectionName, sectionInterface.source().getName(),
				location, properties, links.toArray(new ClassSectionSubSectionOutputLink[links.size()]));

		// Should always be function flow
		ManagedFunctionFlowType<?> flowType = (ManagedFunctionFlowType<?>) annotatedType;

		// Return the input to the section
		return subSection.getSubSectionInput(flowType.getFlowName());
	}

	/**
	 * Obtains the value taking {@link Class} as priority.
	 * 
	 * @param valueClass Value {@link Class}.
	 * @param value      Fallback value if not {@link Class}.
	 * @return Value.
	 */
	private static String getValue(Class<?> valueClass, String value) {
		return (valueClass != null) && (!Void.class.equals(valueClass)) && (!Void.TYPE.equals(valueClass))
				? valueClass.getName()
				: value;
	}

}
