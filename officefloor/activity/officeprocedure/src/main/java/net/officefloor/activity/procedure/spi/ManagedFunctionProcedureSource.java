/*-
 * #%L
 * Procedure
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
