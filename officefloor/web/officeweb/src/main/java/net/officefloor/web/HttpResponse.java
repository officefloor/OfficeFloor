package net.officefloor.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;

/**
 * Annotates an {@link ObjectResponse} parameter to provide additional HTTP
 * response configuration.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Qualifier(nameFactory = HttpResponse.HttpResponseQualifierNameFactory.class)
public @interface HttpResponse {

	/**
	 * {@link QualifierNameFactory} to obtain qualified name for
	 * {@link HttpResponse}.
	 */
	public static class HttpResponseQualifierNameFactory implements QualifierNameFactory<HttpResponse> {
		@Override
		public String getQualifierName(HttpResponse annotation) {
			return String.valueOf(annotation.status());
		}
	}

	/**
	 * Specifies status to use for {@link ObjectResponse}.
	 * 
	 * @return Status to use for {@link ObjectResponse}.
	 */
	int status();

}