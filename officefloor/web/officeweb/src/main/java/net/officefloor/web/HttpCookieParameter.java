package net.officefloor.web;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpCookie;

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;

/**
 * {@link Annotation} to indicate the value is loaded from a {@link HttpCookie}
 * parameter.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Qualifier(nameFactory = HttpCookieParameter.HttpCookieParameterNameFactory.class)
public @interface HttpCookieParameter {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class HttpCookieParameterNameFactory implements QualifierNameFactory<HttpCookieParameter> {

		/**
		 * Obtains the qualifier from name.
		 * 
		 * @param name Name.
		 * @return Qualifier.
		 */
		public static String getQualifier(String name) {
			return HttpCookieParameter.class.getSimpleName() + "_" + name;
		}

		/*
		 * ================== QualifierNameFactory =======================
		 */

		@Override
		public String getQualifierName(HttpCookieParameter annotation) {
			return getQualifier(annotation.value());
		}
	}

	/**
	 * Name of parameter.
	 * 
	 * @return Name parameter.
	 */
	String value();

}