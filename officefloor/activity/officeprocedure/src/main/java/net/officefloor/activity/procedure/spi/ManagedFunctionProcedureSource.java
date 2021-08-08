/*-
 * #%L
 * Procedure
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
