package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParameterClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/*
	 * =================== ClassDependencyManufacturerServiceFactory ===============
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== ClassDependencyManufacturer =======================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if have parameter annotation
		Parameter parameter = context.getDependencyAnnotation(Parameter.class);
		if (parameter == null) {
			return null; // not parameter
		}

		// Is parameter
		Class<?> parameterType = context.getDependencyClass();
		int parameterIndex = context.getExecutableParameterIndex();
		context.addAnnotation(new ParameterAnnotation(parameterType, parameterIndex));

		// Only enriches with annotation
		return null;
	}

}