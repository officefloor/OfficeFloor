package net.officefloor.compile.test.annotation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

/**
 * Utility functionality for annotations.
 * 
 * @author Daniel Sagenschneider
 */
public class AnnotationLoaderUtil {

	/**
	 * Validate have expected annotations.
	 * 
	 * @param messageSuffix       Message suffix to assertion.
	 * @param expectedAnnotations Expected annotation instances.
	 * @param actualAnnotations   Actual annotation instances.
	 */
	public static void validateAnnotations(String messageSuffix, Object[] expectedAnnotations,
			Object[] actualAnnotations) {

		// Verify expected annotations exist
		List<Object> actualAnnotationList = Arrays.asList(actualAnnotations);
		for (Object expectedAnnotation : expectedAnnotations) {

			// Obtain the expected annotation class
			Class<?> expectedAnnotationType = getAnnotationType(expectedAnnotation);

			// Determine if annotation in actual annotations
			boolean isPresent = actualAnnotationList.stream().anyMatch((actualAnnotation) -> {
				Class<?> actualAnnotationType = getAnnotationType(actualAnnotation);
				return expectedAnnotationType.equals(actualAnnotationType);
			});

			// Ensure have expected annotation
			Assert.assertTrue("Expecting annotation " + expectedAnnotationType.getName()
					+ (messageSuffix != null ? " " + messageSuffix : ""), isPresent);
		}
	}

	/**
	 * Obtains the type for annotation.
	 * 
	 * @param annotation Annotation.
	 * @return Type of the annotation.
	 */
	public static Class<?> getAnnotationType(Object annotation) {
		if (annotation instanceof Annotation) {
			Class<?> annotationType = ((Annotation) annotation).annotationType();
			return annotationType != null ? annotationType : annotation.getClass();
		} else if (annotation instanceof Class) {
			return (Class<?>) annotation;
		} else {
			return annotation.getClass();
		}
	}

	/**
	 * All access via static methods.
	 */
	private AnnotationLoaderUtil() {
	}
}