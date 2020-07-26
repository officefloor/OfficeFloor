package net.officefloor.plugin.section.clazz.object.impl;

import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturer;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerContext;
import net.officefloor.plugin.section.clazz.object.ClassSectionObjectManufacturerServiceFactory;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link ClassSectionObjectManufacturer} for
 * {@link VariableManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableSectionClassObjectManufacturer
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

		// Determine if variable
		VariableAnnotation variable = context.getAnnotatedType().getAnnotation(VariableAnnotation.class);
		if (variable != null) {
			// Will augment the variable
			context.flagAugmented();
		}

		// Nothing to link
		return null;
	}

}