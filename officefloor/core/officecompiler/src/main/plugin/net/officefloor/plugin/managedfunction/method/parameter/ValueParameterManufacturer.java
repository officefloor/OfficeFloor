package net.officefloor.plugin.managedfunction.method.parameter;

import java.lang.annotation.Annotation;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link MethodParameterManufacturer} for a {@link Val}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueParameterManufacturer
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/*
	 * =========== MethodParameterManufacturerServiceFactory ===============
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================ MethodParameterManufacturer =================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Determine if Val
		boolean isVal = false;
		Annotation[] annotations = context.getParameterAnnotations();
		for (Annotation annotation : annotations) {
			if (Val.class.equals(annotation.annotationType())) {
				isVal = true;
			}
		}
		if (!isVal) {
			return null; // not value
		}

		// Obtain the variable details
		String qualifier = context.getParameterQualifier();
		String type = VariableManagedObjectSource.type(context.getParameterType().getTypeName());
		String qualifiedName = VariableManagedObjectSource.name(qualifier, type);

		// Add the variable
		int objectIndex = context.addObject(Var.class, (builder) -> {
			builder.setTypeQualifier(qualifiedName);
			builder.setLabel("VAR-" + qualifiedName);
			for (Annotation annotation : annotations) {
				builder.addAnnotation(annotation);
			}
			builder.addAnnotation(new VariableAnnotation(qualifiedName, type));
		});

		// Return value
		return new ValueParameterFactory(objectIndex);
	}
}