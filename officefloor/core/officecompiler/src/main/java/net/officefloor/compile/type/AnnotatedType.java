package net.officefloor.compile.type;

/**
 * <p>
 * Identifies types that may be annotated.
 * <p>
 * Also, provides additional helper methods on the annotated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface AnnotatedType {

	/**
	 * Obtains the annotations.
	 * 
	 * @return Annotations.
	 */
	Object[] getAnnotations();

	/**
	 * Obtains the first annotation matching the type.
	 * 
	 * @param type Type of annotation.
	 * @return First annotation matching the type.
	 */
	@SuppressWarnings("unchecked")
	default <A> A getAnnotation(Class<A> type) {

		// Obtain the annotations
		Object[] annotations = this.getAnnotations();
		if (annotations == null) {
			return null;
		}

		// First pass to find by exact type
		for (Object annotation : annotations) {
			Class<?> annotationClass = annotation.getClass();

			// Determine if exact match
			if (annotationClass.equals(type)) {
				return (A) annotation; // found
			}
		}

		// Next pass to match by sub type
		for (Object annotation : annotations) {
			Class<?> annotationClass = annotation.getClass();

			// Determine if child match
			// (Note: annotations can not be extended)
			if (type.isAssignableFrom(annotationClass)) {
				return (A) annotation;
			}
		}

		// As here, no match for type
		return null;
	}

}