package net.officefloor.plugin.clazz.interrogate.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.Init;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogator;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogatorContext;
import net.officefloor.plugin.clazz.interrogate.ClassInjectionInterrogatorServiceFactory;

/**
 * {@link Init} {@link ClassInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public class InitClassInjectionInterrogator
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

		// Load if annotated with dependency
		AnnotatedElement element = context.getAnnotatedElement();
		if (element.isAnnotationPresent(Init.class)) {

			// Register the post construct
			context.registerPostConstruct((Method) element);
		}
	}

}