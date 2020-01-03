/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide the
 * {@link ManagedFunction} <code>type definition</code>s.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceBuilder {

	/**
	 * Adds a {@link ManagedFunctionTypeBuilder} to this
	 * {@link FunctionNamespaceBuilder} definition.
	 * 
	 * @param <M>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory} to create the
	 *            {@link ManagedFunction}.
	 * @param objectKeysClass
	 *            {@link Enum} providing the keys of the dependent
	 *            {@link Object} instances required by the
	 *            {@link ManagedFunctionTypeBuilder}. This may be
	 *            <code>null</code> if the {@link ManagedFunctionTypeBuilder}
	 *            requires no dependent {@link Object} instances or they are
	 *            {@link Indexed}.
	 * @param flowKeysClass
	 *            {@link Enum} providing the keys of the {@link Flow} instigated
	 *            by the {@link ManagedFunctionTypeBuilder}. This may be
	 *            <code>null</code> if the {@link ManagedFunctionTypeBuilder}
	 *            does not instigate {@link Flow} instances or they are
	 *            {@link Indexed}.
	 * @return {@link ManagedFunctionTypeBuilder} to provide
	 *         <code>type definition</code> of the added
	 *         {@link ManagedFunction}.
	 */
	<M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> addManagedFunctionType(String functionName,
			ManagedFunctionFactory<M, F> functionFactory, Class<M> objectKeysClass, Class<F> flowKeysClass);

}
