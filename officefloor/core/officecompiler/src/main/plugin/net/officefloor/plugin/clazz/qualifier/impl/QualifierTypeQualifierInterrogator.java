package net.officefloor.plugin.clazz.qualifier.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogator;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorServiceFactory;

/**
 * {@link TypeQualifierInterrogator} for the {@link Qualifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class QualifierTypeQualifierInterrogator
		implements TypeQualifierInterrogator, TypeQualifierInterrogatorServiceFactory {

	/*
	 * ================ TypeQualifierInterrogatorServiceFactory ===========
	 */

	@Override
	public TypeQualifierInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== TypeQualifierInterrogator =====================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String interrogate(TypeQualifierInterrogatorContext context) throws Exception {

		// Determine parameter qualifier
		String qualifier = null;
		for (Annotation annotation : context.getAnnotatedElement().getAnnotations()) {

			// Obtain the annotation type
			Class<?> annotationType = annotation.annotationType();

			// Determine if qualifier annotation
			Qualifier qualifierAnnotation = annotationType.getAnnotation(Qualifier.class);
			if (qualifierAnnotation != null) {

				// Allow only one qualifier
				if (qualifier != null) {

					// Determine location
					String location;
					Field field = context.getField();
					if (field != null) {
						location = "Field " + field.getName();
					} else {
						Executable executable = context.getExecutable();
						if (executable instanceof Constructor) {
							location = Constructor.class.getSimpleName();
						} else {
							location = "Method " + executable.getName();
						}
						location += " parameter " + context.getExecutableParameterIndex();
					}

					// Indicate more that one qualifier
					throw new InvalidConfigurationError(
							location + " has more than one " + Qualifier.class.getSimpleName());
				}

				// Obtain the qualifier name factory
				Class<? extends QualifierNameFactory> nameFactoryClass = qualifierAnnotation.nameFactory();
				QualifierNameFactory<Annotation> nameFactory = nameFactoryClass.getDeclaredConstructor().newInstance();

				// Provide type qualifier
				qualifier = nameFactory.getQualifierName(annotation);
			}
		}

		// Return the possible qualifier
		return qualifier;
	}

}