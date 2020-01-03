package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.source.SourceProperties;

/**
 * Configuration of an {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveConfiguration<XS extends ExecutiveSource> {

	/**
	 * Obtains the {@link ExecutiveSource} instance to use.
	 * 
	 * @return {@link ExecutiveSource} instance to use. This may be
	 *         <code>null</code> and therefore the
	 *         {@link #getExecutiveSourceClass()} should be used to obtain the
	 *         {@link ExecutiveSource}.
	 */
	XS getExecutiveSource();

	/**
	 * Obtains the {@link Class} of the {@link ExecutiveSource}.
	 * 
	 * @return {@link Class} of the {@link ExecutiveSource}.
	 */
	Class<XS> getExecutiveSourceClass();

	/**
	 * Obtains the {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 * 
	 * @return {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 */
	SourceProperties getProperties();

}
