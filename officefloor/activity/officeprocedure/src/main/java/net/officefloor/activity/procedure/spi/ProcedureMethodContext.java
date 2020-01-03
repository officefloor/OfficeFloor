package net.officefloor.activity.procedure.spi;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceFactory;

/**
 * Context for the {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureMethodContext {

	/**
	 * Obtains the resource configured to provide the {@link Procedure}.
	 * 
	 * @return Resource configured to provide the {@link Procedure}.
	 */
	String getResource();

	/**
	 * Name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}.
	 */
	String getProcedureName();

	/**
	 * <p>
	 * Overrides the default {@link MethodObjectInstanceFactory}.
	 * <p>
	 * Specifying <code>null</code> indicates a static {@link Method}.
	 * 
	 * @param factory {@link MethodObjectInstanceFactory}.
	 */
	void setMethodObjectInstanceFactory(MethodObjectInstanceFactory factory);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}