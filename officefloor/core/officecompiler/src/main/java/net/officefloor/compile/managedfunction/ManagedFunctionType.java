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
package net.officefloor.compile.managedfunction;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionType<W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionFactory}.
	 * 
	 * @return {@link ManagedFunctionFactory}.
	 */
	ManagedFunctionFactory<W, M, F> getManagedFunctionFactory();

	/**
	 * Obtains the differentiator.
	 * 
	 * @return Differentiator.
	 * 
	 * @see ManagedFunctionBuilder#setDifferentiator(Object)
	 */
	Object getDifferentiator();

	/**
	 * Obtains the type of {@link Object} returned from the {@link ManagedFunction} that is
	 * to be used as the argument to the next {@link ManagedFunction}.
	 * 
	 * @return Return type of the {@link ManagedFunction}.
	 */
	Class<?> getReturnType();

	/**
	 * Obtains the {@link Enum} providing the keys for the dependent
	 * {@link Object} instances.
	 * 
	 * @return {@link Enum} providing the dependent {@link Object} keys or
	 *         <code>null</code> if {@link Indexed} or no dependencies.
	 */
	Class<M> getObjectKeyClass();

	/**
	 * Obtains the {@link ManagedFunctionObjectType} definitions for the dependent
	 * {@link Object} instances required by the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionObjectType} definitions for the dependent
	 *         {@link Object} instances required by the {@link ManagedFunction}.
	 */
	ManagedFunctionObjectType<M>[] getObjectTypes();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow}
	 * instances instigated by the {@link ManagedFunction}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated
	 *         {@link Flow} instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link ManagedFunctionFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionFlowType} definitions for the possible {@link Flow}
	 *         instances instigated by the {@link ManagedFunction}.
	 */
	ManagedFunctionFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link ManagedFunctionEscalationType} definitions for the possible
	 * {@link EscalationFlow} instances by the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunctionEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link ManagedFunction}.
	 */
	ManagedFunctionEscalationType[] getEscalationTypes();

}