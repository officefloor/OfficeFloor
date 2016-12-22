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
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide a
 * <code>type definition</code> of the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionTypeBuilder<D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Specifies the differentiator.
	 * 
	 * @param differentiator
	 *            Differentiator.
	 * 
	 * @see ManagedFunctionBuilder#setDifferentiator(Object)
	 */
	void setDifferentiator(Object differentiator);

	/**
	 * Specifies the type of {@link Object} returned from the
	 * {@link ManagedFunction} that is to be used as the argument to the next
	 * {@link ManagedFunction}.
	 * 
	 * @param returnType
	 *            Return type of the {@link ManagedFunction}.
	 */
	void setReturnType(Class<?> returnType);

	/**
	 * <p>
	 * Adds a {@link ManagedFunctionObjectTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * <p>
	 * Should the dependent {@link Object} instances be {@link Indexed}, the
	 * order they are added is the order of indexing (starting at 0).
	 * 
	 * @param objectType
	 *            Type of the dependent {@link Object}.
	 * @return {@link ManagedFunctionObjectTypeBuilder} to provide the
	 *         <code>type definition</code> of the added dependent
	 *         {@link Object}.
	 */
	ManagedFunctionObjectTypeBuilder<D> addObject(Class<?> objectType);

	/**
	 * <p>
	 * Adds a {@link ManagedFunctionFlowTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * <p>
	 * Should the {@link Flow} instigation be {@link Indexed}, the order they
	 * are added is the order of indexing (starting at 0).
	 * 
	 * @return {@link ManagedFunctionFlowTypeBuilder} to provide the
	 *         <code>type definition</code> of the possible instigated
	 *         {@link Flow} by the {@link ManagedFunction}.
	 */
	ManagedFunctionFlowTypeBuilder<F> addFlow();

	/**
	 * Adds a {@link ManagedFunctionEscalationTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition.
	 * 
	 * @param <E>
	 *            {@link Escalation} type.
	 * @param escalationType
	 *            Type to be handled by an {@link EscalationFlow}.
	 * @return {@link ManagedFunctionEscalationTypeBuilder} to provide the
	 *         <code>type definition</code>.
	 */
	<E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(Class<E> escalationType);

}