package net.officefloor.plugin.section.clazz.parameter.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogator;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorContext;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorServiceFactory;

/**
 * {@link ClassSectionParameterInterrogator} for the
 * {@link ParameterAnnotation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterAnnotationClassSectionParameterInterrogator
		implements ClassSectionParameterInterrogator, ClassSectionParameterInterrogatorServiceFactory {

	/*
	 * ============= ClassSectionParameterInterrogatorServiceFactory =============
	 */

	@Override
	public ClassSectionParameterInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== ClassSectionParameterInterrogator=======================
	 */

	@Override
	public boolean isParameter(ClassSectionParameterInterrogatorContext context) throws Exception {

		// Determine if have parameter annotation
		ParameterAnnotation parameter = context.getManagedFunctionObjectType().getAnnotation(ParameterAnnotation.class);
		return parameter != null;
	}

}