package net.officefloor.web.template.section;

import net.officefloor.web.HttpInputPath;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Annotation identifying a redirect to the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateRedirectAnnotation {

	/**
	 * Type provided to the redirect to source values to construct the
	 * {@link HttpInputPath}.
	 */
	private final Class<?> valuesType;

	/**
	 * Instantiate.
	 * 
	 * @param valuesType
	 *            Type provided to the redirect to source values to construct
	 *            the {@link HttpInputPath}. May be <code>null</code> if no
	 *            values are required.
	 */
	public WebTemplateRedirectAnnotation(Class<?> valuesType) {
		this.valuesType = valuesType;
	}

	/**
	 * Obtains the type provided to the redirect to source values to construct
	 * the {@link HttpInputPath}.
	 * 
	 * @return Type provided to the redirect to source values to construct the
	 *         {@link HttpInputPath}. May be <code>null</code>.
	 */
	public Class<?> getValuesType() {
		return this.valuesType;
	}

}