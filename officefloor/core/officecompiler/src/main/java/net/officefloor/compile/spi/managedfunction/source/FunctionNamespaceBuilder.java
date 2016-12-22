/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide the
 * {@link ManagedFunction} <code>type definition</code>s.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionNamespaceBuilder<W extends Work> {

	/**
	 * Specifies the {@link WorkFactory} to create the {@link Work}.
	 * 
	 * @param workFactory
	 *            {@link WorkFactory}.
	 */
	@Deprecated // functions don't have state
	void setWorkFactory(WorkFactory<W> workFactory);

	/**
	 * Adds a {@link ManagedFunctionTypeBuilder} to this
	 * {@link FunctionNamespaceBuilder} definition.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param taskName
	 *            Name of the {@link ManagedFunction}.
	 * @param taskFactory
	 *            {@link ManagedFunctionFactory} to create the
	 *            {@link ManagedFunction}.
	 * @param objectKeysClass
	 *            {@link Enum} providing the keys of the dependent
	 *            {@link Object} instances required by the
	 *            {@link ManagedFunctionTypeBuilder}. This may be <code>null</code> if the
	 *            {@link ManagedFunctionTypeBuilder} requires no dependent {@link Object}
	 *            instances or they are {@link Indexed}.
	 * @param flowKeysClass
	 *            {@link Enum} providing the keys of the {@link Flow} instigated
	 *            by the {@link ManagedFunctionTypeBuilder}. This may be <code>null</code>
	 *            if the {@link ManagedFunctionTypeBuilder} does not instigate {@link Flow}
	 *            instances or they are {@link Indexed}.
	 * @return {@link ManagedFunctionTypeBuilder} to provide <code>type definition</code>
	 *         of the added {@link ManagedFunction}.
	 */
	<D extends Enum<D>, F extends Enum<F>> ManagedFunctionTypeBuilder<D, F> addManagedFunctionType(String taskName,
			ManagedFunctionFactory<? super W, D, F> taskFactory, Class<D> objectKeysClass, Class<F> flowKeysClass);

}