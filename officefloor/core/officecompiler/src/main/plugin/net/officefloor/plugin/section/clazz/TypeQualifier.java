package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link TypeQualification} of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
public @interface TypeQualifier {

	/**
	 * Qualifier.
	 * 
	 * @return Qualifier.
	 */
	Class<? extends Annotation> qualifier() default TypeQualifier.class;

	/**
	 * Type.
	 * 
	 * @return Type.
	 */
	Class<?> type();

}