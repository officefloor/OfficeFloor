package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;

/**
 * Determines the {@link Qualifier} name from the {@link Qualifier} attributes.
 * 
 * @author Daniel Sagenschneider
 */
public interface QualifierNameFactory<A extends Annotation> {

	/**
	 * Obtains the {@link Qualifier} name from the {@link Annotation}.
	 * 
	 * @param annotation
	 *            {@link Annotation} containing attributes to aid determining
	 *            the name.
	 * @return Qualified name.
	 */
	String getQualifierName(A annotation);

}