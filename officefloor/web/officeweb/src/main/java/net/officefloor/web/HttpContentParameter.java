package net.officefloor.web;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;
import net.officefloor.server.http.HttpRequest;

/**
 * {@link Annotation} to indicate the value is loaded from content of
 * {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Qualifier(nameFactory = HttpContentParameter.HttpContentParameterNameFactory.class)
public @interface HttpContentParameter {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class HttpContentParameterNameFactory implements QualifierNameFactory<HttpContentParameter> {

		/**
		 * Obtains the qualifier from name.
		 * 
		 * @param name Name.
		 * @return Qualifier.
		 */
		public static String getQualifier(String name) {
			return HttpContentParameter.class.getSimpleName() + "_" + name;
		}

		/*
		 * ================== QualifierNameFactory =======================
		 */

		@Override
		public String getQualifierName(HttpContentParameter annotation) {
			return getQualifier(annotation.value());
		}
	}

	/**
	 * Name of the parameter.
	 * 
	 * @return Name of the parameter.
	 */
	String value();

}