package net.officefloor.plugin.clazz.constructor.impl;

import java.lang.reflect.Constructor;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogator;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogatorContext;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogatorServiceFactory;

/**
 * {@link ClassConstructorInterrogator} for {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyClassConstructorInterrogator
		implements ClassConstructorInterrogator, ClassConstructorInterrogatorServiceFactory {

	/*
	 * =============== ClassConstructorInterrogatorServiceFactory ================
	 */

	@Override
	public ClassConstructorInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ClassConstructorInterrogator =======================
	 */

	@Override
	public Constructor<?> interrogate(ClassConstructorInterrogatorContext context) throws Exception {

		// Obtain the class
		Class<?> clazz = context.getObjectClass();

		// Obtain the constructor
		Constructor<?>[] constructors = clazz.getConstructors();
		switch (constructors.length) {
		case 0:
			context.setErrorInformation("No public constructor available for " + clazz.getName());

		case 1:
			// Use only constructor
			return constructors[0];

		default:
			// Search for annotated constructor
			Constructor<?> annotatedConstructor = null;
			for (Constructor<?> constructor : constructors) {
				if (constructor.isAnnotationPresent(Dependency.class)) {

					// Ensure no other annotated constructor
					if (annotatedConstructor != null) {
						context.setErrorInformation("Two constructors annotated with "
								+ Dependency.class.getSimpleName() + " for " + clazz.getName());
						return null; // constructor not found
					}

					// Flag as constructor to use
					annotatedConstructor = constructor;
				}
			}
			if (annotatedConstructor != null) {
				return annotatedConstructor; // found constructor to use

			} else {
				// No annotated constructor
				context.setErrorInformation("Must specify one of the constructors with "
						+ Dependency.class.getSimpleName() + " for " + clazz.getName());
				return null; // constructor not found
			}
		}
	}

}