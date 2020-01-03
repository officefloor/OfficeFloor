package net.officefloor.frame.api.executive.source;

import net.officefloor.frame.api.executive.Executive;

/**
 * Source to obtain the {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	ExecutiveSourceSpecification getSpecification();

	/**
	 * Creates the {@link Executive}.
	 * 
	 * @param context
	 *            {@link ExecutiveSourceContext}.
	 * @return {@link Executive}.
	 * @throws Exception
	 *             If fails to configure the {@link ExecutiveSource}.
	 */
	Executive createExecutive(ExecutiveSourceContext context) throws Exception;

}