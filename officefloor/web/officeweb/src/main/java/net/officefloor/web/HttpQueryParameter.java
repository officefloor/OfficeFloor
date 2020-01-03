package net.officefloor.web;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;

/**
 * {@link Annotation} to indicate the value is loaded from a query parameter.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Qualifier(nameFactory = HttpQueryParameter.HttpQueryParameterNameFactory.class)
public @interface HttpQueryParameter {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class HttpQueryParameterNameFactory implements QualifierNameFactory<HttpQueryParameter> {

		/**
		 * Obtains the qualifier from name.
		 * 
		 * @param name Name.
		 * @return Qualifier.
		 */
		public static String getQualifier(String name) {
			return HttpQueryParameter.class.getSimpleName() + "_" + name;
		}

		/*
		 * ================== QualifierNameFactory =======================
		 */

		@Override
		public String getQualifierName(HttpQueryParameter annotation) {
			return getQualifier(annotation.value());
		}
	}

	/**
	 * Allows specifying the query parameter name.
	 * 
	 * @return Query parameter name.
	 */
	String value();

}