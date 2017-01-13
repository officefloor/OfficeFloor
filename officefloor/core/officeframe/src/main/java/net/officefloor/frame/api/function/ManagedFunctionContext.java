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
package net.officefloor.frame.api.function;

import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context in which the {@link ManagedFunction} is done.
 * 
 * @param O
 *            Type providing the keys for the dependency {@link ManagedObject}
 *            instances. Dependencies may either be:
 *            <ol>
 *            <li>{@link Object} of a {@link ManagedObject}</li>
 *            <li>Parameter for the {@link ManagedFunction}</li>
 *            </ol>
 * @param F
 *            Type providing the keys to the possible {@link Flow} instances
 *            instigated by this {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionContext<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the dependency object.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @return Dependency object.
	 */
	Object getObject(O key);

	/**
	 * <p>
	 * Similar to {@link #getObject(Enum)} except allows dynamically obtaining
	 * the dependencies.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * dependencies available.
	 * 
	 * @param dependencyIndex
	 *            Index identifying the dependency.
	 * @return Dependency object.
	 */
	Object getObject(int dependencyIndex);

	/**
	 * Instigates a {@link Flow} to be run.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link ManagedFunction} of the
	 *            {@link Flow} instigated.
	 * @param callback
	 *            Optional {@link FlowCallback} that is invoked on completion of
	 *            the {@link Flow}.
	 */
	void doFlow(F key, Object parameter, FlowCallback callback);

	/**
	 * <p>
	 * Similar to {@link #doFlow(Enum, Object)} except that allows dynamic
	 * instigation of {@link Flow}.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * {@link Flow} instances available.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter that will be available for the first
	 *            {@link ManagedFunction} of the {@link Flow} to be run.
	 * @param callback
	 *            Optional {@link FlowCallback} that is invoked on completion of
	 *            the {@link Flow}.
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

	/**
	 * <p>
	 * Invokes a {@link Flow} by dynamically naming the initial
	 * {@link ManagedFunction} of the {@link Flow}.
	 * <p>
	 * This method should not be preferred, as the other
	 * <code>doFlow(...)</code> methods are compile safe. This method however
	 * provides the similar functionality as per reflection - powerful yet
	 * compile unsafe.
	 * <p>
	 * The {@link ManagedFunction} reflective meta-data may be obtained from the
	 * {@link Office} made available via the {@link OfficeAwareManagedFunctionFactory}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction} within the {@link Work}.
	 * @param parameter
	 *            Parameter to the task. May be <code>null</code>.
	 * @param callback
	 *            Optional {@link FlowCallback} that is invoked on completion of
	 *            the {@link Flow}.
	 * @throws UnknownFunctionException
	 *             Should no {@link ManagedFunction} be in the {@link Office} by
	 *             the name.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter be an invalid type for the
	 *             {@link ManagedFunction}.
	 */
	void doFlow(String functionkName, Object parameter, FlowCallback callback)
			throws UnknownFunctionException, InvalidParameterTypeException;

}