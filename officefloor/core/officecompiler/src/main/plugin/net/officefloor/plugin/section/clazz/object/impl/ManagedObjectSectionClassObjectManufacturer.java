package net.officefloor.plugin.section.clazz.object.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.PropertyValue;
import net.officefloor.plugin.section.clazz.TypeQualifier;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturer;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.object.ClassSectionTypeQualifier;

/**
 * {@link ClassSectionObjectManufacturer} for {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSectionClassObjectManufacturer
		implements ClassSectionObjectManufacturer, ClassSectionObjectManufacturerServiceFactory {

	/*
	 * ============== SectionClassObjectManufacturerServiceFactory ==============
	 */

	@Override
	public ClassSectionObjectManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ===================== SectionClassObjectManufacturer =====================
	 */

	@Override
	public SectionDependencyObjectNode createObject(ClassSectionObjectManufacturerContext context) throws Exception {

		// Determine if managed object
		ManagedObject moAnnotation = context.getAnnotatedType().getAnnotation(ManagedObject.class);
		if (moAnnotation == null) {
			return null; // not managed object
		}

		// Load the properties
		PropertyList properties = context.getSourceContext().createPropertyList();
		for (PropertyValue property : moAnnotation.properties()) {
			String value = ("".equals(property.value()) ? property.valueClass().getName() : property.value());
			properties.addProperty(property.name()).setValue(value);
		}

		// Obtain the type qualifiers for managed object
		List<ClassSectionTypeQualifier> typeQualifiers = new LinkedList<>();
		for (TypeQualifier qualifierAnnotation : moAnnotation.qualifiers()) {
			Class<?> qualifierClass = qualifierAnnotation.qualifier();
			if (TypeQualifier.class.equals(qualifierClass)) {
				// No qualifier (as default value)
				qualifierClass = null;
			}
			String qualifier = (qualifierClass == null ? null : qualifierClass.getName());
			Class<?> type = qualifierAnnotation.type();
			typeQualifiers.add(context.createTypeQualifier(qualifier, type));
		}

		// Provide the managed object
		return context.getOrCreateManagedObject(moAnnotation.source().getName(), properties,
				typeQualifiers.toArray(new ClassSectionTypeQualifier[typeQualifiers.size()]));
	}

}