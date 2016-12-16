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
package net.officefloor.frame.api.execute;

import net.officefloor.frame.api.build.OfficeAwareWorkFactory;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context in which the {@link Task} is done.
 * 
 * @param W
 *            Specific {@link Work}.
 * @param D
 *            Type providing the keys for the dependencies. Dependencies may
 *            either be:
 *            <ol>
 *            <li>{@link Object} of a {@link ManagedObject}</li>
 *            <li>Parameter for the {@link Task}</li>
 *            </ol>
 * @param F
 *            Type providing the keys to the possible {@link Flow} instances
 *            instigated by this {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskContext<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the {@link Work} of the {@link Task}.
	 * 
	 * @return {@link Work} of the {@link Task}.
	 */
	@Deprecated // state to be obtained from managed object
	W getWork();

	/**
	 * Obtains the dependency object.
	 * 
	 * @param key
	 *            Key identifying the dependency.
	 * @return Dependency object.
	 */
	Object getObject(D key);

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
	 *            Parameter for the first {@link Task} of the {@link Flow}
	 *            instigated.
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
	 *            Parameter that will be available for the first {@link Task} of
	 *            the {@link Flow} to be run.
	 * @param callback
	 *            Optional {@link FlowCallback} that is invoked on completion of
	 *            the {@link Flow}.
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

	/**
	 * <p>
	 * Invokes a {@link Flow} by dynamically naming the initial {@link Task} of
	 * the {@link Flow}.
	 * <p>
	 * This method should not be preferred, as the other
	 * <code>doFlow(...)</code> methods are compile safe. This method however
	 * provides the similar functionality as per reflection - powerful yet
	 * compile unsafe.
	 * <p>
	 * The {@link Work} and {@link Task} reflective meta-data may be obtained
	 * from the {@link Office} made available via the
	 * {@link OfficeAwareWorkFactory}.
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the {@link Task}.
	 * @param taskName
	 *            Name of {@link Task} within the {@link Work}.
	 * @param parameter
	 *            Parameter to the task. May be <code>null</code>.
	 * @param callback
	 *            Optional {@link FlowCallback} that is invoked on completion of
	 *            the {@link Flow}.
	 * @throws UnknownWorkException
	 *             Should no {@link Work} be known by the name.
	 * @throws UnknownTaskException
	 *             Should no {@link Task} by the name be contained under the
	 *             {@link Work}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter be an invalid type for the {@link Task}.
	 * 
	 * @see OfficeAwareWorkFactory
	 */
	void doFlow(String workName, String taskName, Object parameter, FlowCallback callback)
			throws UnknownWorkException, UnknownTaskException, InvalidParameterTypeException;

}