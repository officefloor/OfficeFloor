package net.officefloor.plugin.section.clazz.parameter.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogator;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorContext;
import net.officefloor.plugin.section.clazz.parameter.ClassSectionParameterInterrogatorServiceFactory;

/**
 * {@link ClassSectionParameterInterrogator} for the {@link Parameter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterClassSectionParameterInterrogator
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
		Parameter parameter = context.getManagedFunctionObjectType().getAnnotation(Parameter.class);
		return parameter != null;
	}

}