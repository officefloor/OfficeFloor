package net.officefloor.plugin.managedfunction.method.parameter;

import java.lang.annotation.Annotation;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link MethodParameterManufacturer} for a variable.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractVariableParameterManufacturer
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/**
	 * Obtains the parameter {@link Class}.
	 * 
	 * @return Parameter {@link Class}.
	 */
	protected abstract Class<?> getParameterClass();

	/**
	 * Creates the {@link MethodParameterFactory}.
	 * 
	 * @param objectIndex Index of the variable.
	 * @return {@link MethodParameterFactory}.
	 */
	protected abstract MethodParameterFactory createMethodParameterFactory(int objectIndex);

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

		// Determine if appropriate type
		if (!this.getParameterClass().equals(context.getParameterClass())) {
			return null; // not appropriate type
		}

		// Obtain the variable details
		String qualifier = context.getParameterQualifier();
		String type = VariableManagedObjectSource.extractVariableType(context.getParameterType());
		String qualifiedName = VariableManagedObjectSource.name(qualifier, type);

		// Add the variable
		int objectIndex = context.addObject(Var.class, (builder) -> {
			builder.setTypeQualifier(qualifiedName);
			builder.setLabel("VAR-" + qualifiedName);
			for (Annotation annotation : context.getParameterAnnotations()) {
				builder.addAnnotation(annotation);
			}
			builder.addAnnotation(new VariableAnnotation(qualifiedName, type));
		});

		// Return variable
		return this.createMethodParameterFactory(objectIndex);
	}
}