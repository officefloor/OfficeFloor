package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables qualifying dependencies by {@link Annotation} instances.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Qualifier {

	/**
	 * {@link QualifierNameFactory} to derive the name from the
	 * {@link Annotation}.
	 * 
	 * @return {@link QualifierNameFactory} {@link Class} to be instantiated by
	 *         default constructor.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends QualifierNameFactory> nameFactory() default ClassNameQualifierNameFactory.class;

}