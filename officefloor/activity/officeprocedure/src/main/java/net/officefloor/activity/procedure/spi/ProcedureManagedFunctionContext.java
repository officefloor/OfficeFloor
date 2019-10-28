/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.procedure.spi;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link ManagedFunctionProcedureService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureManagedFunctionContext {

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
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

	/**
	 * Sets the {@link ManagedFunctionTypeBuilder} for the {@link Procedure}.
	 * 
	 * @param <M>             Dependency key type.
	 * @param <F>             Flow key type.
	 * @param functionFactory {@link ManagedFunctionFactory} to create the
	 *                        {@link ManagedFunction}.
	 * @param objectKeysClass {@link Enum} providing the keys of the dependent
	 *                        {@link Object} instances required by the
	 *                        {@link ManagedFunctionTypeBuilder}. This may be
	 *                        <code>null</code> if the
	 *                        {@link ManagedFunctionTypeBuilder} requires no
	 *                        dependent {@link Object} instances or they are
	 *                        {@link Indexed}.
	 * @param flowKeysClass   {@link Enum} providing the keys of the {@link Flow}
	 *                        instigated by the {@link ManagedFunctionTypeBuilder}.
	 *                        This may be <code>null</code> if the
	 *                        {@link ManagedFunctionTypeBuilder} does not instigate
	 *                        {@link Flow} instances or they are {@link Indexed}.
	 * @return {@link ManagedFunctionTypeBuilder} to provide
	 *         <code>type definition</code> of the added {@link ManagedFunction}.
	 */
	<M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> setManagedFunction(
			ManagedFunctionFactory<M, F> functionFactory, Class<M> objectKeysClass, Class<F> flowKeysClass);

}