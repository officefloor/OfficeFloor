/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.mbean;

import java.lang.reflect.Proxy;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link Proxy} interface for the {@link OfficeFloor} MBean.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorMBean {

	/**
	 * Obtain the names of the {@link Office} instances within the
	 * {@link OfficeFloor}.
	 * 
	 * @return Names of the {@link Office} instances within the
	 *         {@link OfficeFloor}.
	 */
	String[] getOfficeNames();

	/**
	 * Obtains the names of the {@link ManagedFunction} instances within the
	 * {@link Office}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return Names of the {@link ManagedFunction} instances within the
	 *         {@link Office}.
	 */
	String[] getManagedFunctionNames(String officeName);

	/**
	 * Obtains the parameter type for the {@link ManagedFunction}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return Parameter type for the {@link ManagedFunction}. May be
	 *         <code>null</code> if no parameter for {@link ManagedFunction}.
	 */
	String getManagedFunctionParameterType(String officeName, String functionName);

	/**
	 * Invokes the {@link ManagedFunction}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction} within the {@link Office}.
	 * @param parameter
	 *            Optional parameter for the {@link ManagedFunction}. May be
	 *            <code>null</code>.
	 */
	void invokeFunction(String officeName, String functionName, String parameter);

	/**
	 * Closes the {@link OfficeFloor}.
	 */
	void closeOfficeFloor();

}
