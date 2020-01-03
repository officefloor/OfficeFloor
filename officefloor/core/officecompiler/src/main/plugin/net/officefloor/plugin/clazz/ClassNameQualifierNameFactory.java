package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;

/**
 * {@link QualifierNameFactory} providing the fully qualified {@link Class}
 * name.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassNameQualifierNameFactory implements QualifierNameFactory<Annotation> {

	/*
	 * ================ QualifierNameFactory ====================
	 */

	@Override
	public String getQualifierName(Annotation annotation) {
		return annotation.annotationType().getName();
	}
}