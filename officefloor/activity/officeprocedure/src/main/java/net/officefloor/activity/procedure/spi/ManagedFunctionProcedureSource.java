package net.officefloor.activity.procedure.spi;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * <p>
 * {@link ProcedureSource} that enables full ability to build the
 * {@link ManagedFunction}.
 * <p>
 * In majority of cases, providing a {@link Method} is adequate for running on
 * the JVM. However, there are script engines and other executions that are not
 * represented by {@link Method} instances. Extending
 * {@link ManagedFunctionProcedureSource} rather than {@link ProcedureSource}
 * enables taking full control of building the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionProcedureSource extends ProcedureSource {

	/**
	 * Loads the {@link ManagedFunction} for the {@link Procedure}.
	 * 
	 * @param context {@link ProcedureManagedFunctionContext}.
	 * @throws Exception If fails to load the {@link ManagedFunction}.
	 */
	void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception;

	/*
	 * ===================== ProcedureSource ======================
	 */

	@Override
	default Method loadMethod(ProcedureMethodContext context) throws IllegalStateException {
		throw new IllegalStateException(
				"loadMethod should not be called for " + ManagedFunctionProcedureSource.class.getSimpleName());
	}

}