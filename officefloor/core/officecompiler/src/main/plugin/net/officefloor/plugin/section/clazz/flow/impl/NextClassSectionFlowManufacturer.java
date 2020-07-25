package net.officefloor.plugin.section.clazz.flow.impl;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturer;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerContext;
import net.officefloor.plugin.section.clazz.flow.ClassSectionFlowManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFlow;

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
	public ClassSectionFlow createFlow(ClassSectionFlowManufacturerContext context) throws Exception {

		// Obtain the possible next
		AnnotatedType annotatedType = context.getAnnotatedType();
		Next next = annotatedType.getAnnotation(Next.class);
		if (next == null) {
			return null; // no next
		}

		// Obtain the function type (as must be managed function)
		ManagedFunctionType<?, ?> functionType = (ManagedFunctionType<?, ?>) annotatedType;

		// Obtain the argument type
		Class<?> returnType = functionType.getReturnType();
		Class<?> argumentType = ((returnType == null) || (void.class.equals(returnType))
				|| (Void.TYPE.equals(returnType))) ? null : returnType;

		// Obtain the next flow sink
		return context.getFlow(next.value(), argumentType != null ? argumentType.getName() : null);
	}

}