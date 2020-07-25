package net.officefloor.plugin.clazz.interrogate.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogator;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogatorContext;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogatorServiceFactory;

/**
 * {@link FlowInterface} {@link ClassInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceClassInjectionInterrogator
		implements ClassInjectionInterrogator, ClassInjectionInterrogatorServiceFactory {

	/*
	 * ================ ClassInjectionInterrogatorServiceFactory ==========
	 */

	@Override
	public ClassInjectionInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== ClassInjectionInterrogator ====================
	 */

	@Override
	public void interrogate(ClassInjectionInterrogatorContext context) throws Exception {

		// Load if flow interface field
		// (Constructors/Methods identified via other means)
		AnnotatedElement element = context.getAnnotatedElement();
		if (element instanceof Field) {
			Field field = (Field) element;

			// Determine if field flow interface
			if (field.getType().isAnnotationPresent(FlowInterface.class)) {

				// Register the injection of dependency
				context.registerInjectionPoint(field);
			}
		}
	}

}