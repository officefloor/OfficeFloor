package net.officefloor.jdbc.decorate;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory for the creation of a {@link ConnectionDecorator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionDecoratorFactory {

	/**
	 * Creates the {@link ConnectionDecorator}.
	 * 
	 * @param context {@link SourceContext} to configure the
	 *                {@link ConnectionDecorator}.
	 * @return {@link ConnectionDecorator}.
	 * @throws Exception If fails to create the {@link ConnectionDecorator}.
	 */
	ConnectionDecorator createConnectionDecorator(SourceContext context) throws Exception;

}