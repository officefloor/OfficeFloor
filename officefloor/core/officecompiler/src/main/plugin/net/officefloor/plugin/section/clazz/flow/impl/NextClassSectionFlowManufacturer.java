package net.officefloor.plugin.section.clazz.flow.impl;

import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.NextAnnotation;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;

/**
 * {@link ClassSectionFlowManufacturer} for {@link Next}.
 * 
 * @author Daniel Sagenschneider
 */
public class NextClassSectionFlowManufacturer
		implements ClassSectionFlowManufacturer, ClassSectionFlowManufacturerServiceFactory {

	/*
	 * ============== ClassSectionFlowManufacturerServiceFactory ==============
	 */

	@Override
	public ClassSectionFlowManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== ClassSectionFlowManufacturer ======================
	 */

	@Override
	public SectionFlowSinkNode createFlowSink(ClassSectionFlowManufacturerContext context) throws Exception {

		// Obtain the possible next
		NextAnnotation next = context.getAnnotatedType().getAnnotation(NextAnnotation.class);
		if (next == null) {
			return null; // no next
		}

		// Obtain the next flow sink
		Class<?> argumentType = next.getArgumentType();
		return context.getFlowSink(next.getNextName(), argumentType != null ? argumentType.getName() : null);
	}

}