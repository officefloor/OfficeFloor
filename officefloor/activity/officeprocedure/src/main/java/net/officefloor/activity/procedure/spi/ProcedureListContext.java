package net.officefloor.activity.procedure.spi;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for listing the {@link Procedure} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureListContext {

	/**
	 * Obtains the resource to list the {@link Procedure} instances.
	 * 
	 * @return Resource to list the {@link Procedure} instances.
	 */
	String getResource();

	/**
	 * Adds an available {@link Procedure}.
	 * 
	 * @param procedureName Name of the {@link Procedure}. May be <code>null</code>
	 *                      to indicate for manual selection of {@link Procedure}.
	 * @return {@link ProcedureSpecification} to detail requirements for the
	 *         {@link Procedure}.
	 */
	ProcedureSpecification addProcedure(String procedureName);

	/**
	 * <p>
	 * Obtains the {@link SourceContext}.
	 * <p>
	 * Note that the {@link Property} values will not be available from this
	 * {@link SourceContext}. It is typically only provided to enable to load
	 * resource as a {@link Class}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}