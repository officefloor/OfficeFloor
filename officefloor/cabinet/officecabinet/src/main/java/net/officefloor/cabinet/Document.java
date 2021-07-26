package net.officefloor.cabinet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a {@link Class} to represent a {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Document {
		
	/**
	 * Allows dynamic naming of {@link Document}.
	 */
	@FunctionalInterface
	public static interface DocumentNamer {

		/**
		 * Obtains the name for the {@link Document}.
		 * 
		 * @param context {@link DocumentNameContext}.
		 * @return Name for the {@link Document}.
		 * @throws Exception If fails to name the {@link Document}.
		 */
		String getName(DocumentNameContext context) throws Exception;
	}

	/**
	 * Context for the {@link DocumentNamer}.
	 */
	public static interface DocumentNameContext {

		/**
		 * Obtains {@link Document} {@link Annotation} on {@link Class}.
		 * 
		 * @return {@link Document} {@link Annotation} on {@link Class}.
		 */
		Document getDocument();

		/**
		 * Obtains the {@link Class} of the {@link Document}.
		 * 
		 * @return {@link Class} of the {@link Document}.
		 */
		Class<?> getDocumentClass();
	}

	/**
	 * Default {@link DocumentNamer} that uses the simple name of the {@link Class}.
	 */
	public static class SimpleClassNameDocumentNamer implements DocumentNamer {

		/*
		 * ===================== DocumentNamer =============================
		 */

		@Override
		public String getName(DocumentNameContext context) throws Exception {
			String configuredName = context.getDocument().name();
			return ((configuredName == null) || (configuredName.trim().length() == 0))
					? context.getDocumentClass().getSimpleName()
					: configuredName;
		}
	}

	/**
	 * Custom {@link DocumentNamer} to name the {@link Document}.
	 * 
	 * @return {@link DocumentNamer} to name the {@link Document}.
	 */
	Class<? extends DocumentNamer> documentNamer() default SimpleClassNameDocumentNamer.class;

	/**
	 * Obtains the name of the document in cloud store.
	 * 
	 * @return Name of the document in cloud store.
	 */
	String name() default "";

}