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

package net.officefloor.plugin.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Meta-data of a {@link Method} on a {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassFlowMethodMetaData {

	/**
	 * Type declaring the {@link Method} of this flow.
	 */
	private final Class<?> flowType;

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * Indicates whether to spawn in {@link ThreadState}.
	 */
	private final boolean isSpawn;

	/**
	 * Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	private final int flowIndex;

	/**
	 * Parameter type for the {@link Flow}. Will be <code>null</code> if no
	 * parameter.
	 */
	private final Class<?> parameterType;

	/**
	 * Flag indicating if {@link FlowCallback} for the {@link Flow}.
	 */
	private final boolean isFlowCallback;

	/**
	 * Initiate.
	 * 
	 * @param flowType       Type declaring the {@link Method} of this flow.
	 * @param method         {@link Method}.
	 * @param isSpawn        Indicates whether to spawn in {@link ThreadState}.
	 * @param flowIndex      Index of the {@link Flow} to invoke for this
	 *                       {@link Method}.
	 * @param parameterType  Parameter type for the {@link Flow}. Will be
	 *                       <code>null</code> if no parameter.
	 * @param isFlowCallback <code>true</code> if last parameter is
	 *                       {@link FlowCallback}.
	 */
	public ClassFlowMethodMetaData(Class<?> flowType, Method method, boolean isSpawn, int flowIndex,
			Class<?> parameterType, boolean isFlowCallback) {
		this.flowType = flowType;
		this.method = method;
		this.isSpawn = isSpawn;
		this.flowIndex = flowIndex;
		this.parameterType = parameterType;
		this.isFlowCallback = isFlowCallback;
	}

	/**
	 * Obtains the Type declaring the {@link Method} of this flow.
	 * 
	 * @return Type declaring the {@link Method} of this flow.
	 */
	public Class<?> getFlowType() {
		return this.flowType;
	}

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Indicates whether to spawn in {@link ThreadState}.
	 * 
	 * @return <code>true</code> to spawn in {@link ThreadState}.
	 */
	public boolean isSpawn() {
		return this.isSpawn;
	}

	/**
	 * Obtains the index of the {@link Flow} to invoke for this {@link Method}.
	 * 
	 * @return Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	public int getFlowIndex() {
		return this.flowIndex;
	}

	/**
	 * Indicates if parameter for the {@link Flow}.
	 * 
	 * @return <code>true</code> if parameter for the {@link Flow}.
	 */
	public boolean isParameter() {
		return (this.parameterType != null);
	}

	/**
	 * Obtains the parameter type for the {@link Flow}. Will be <code>null</code> if
	 * no parameter.
	 * 
	 * @return Parameter type for the {@link Flow}. Will be <code>null</code> if no
	 *         parameter.
	 * 
	 */
	public Class<?> getParameterType() {
		return this.parameterType;
	}

	/**
	 * Flags if {@link FlowCallback}.
	 * 
	 * @return <code>true</code> if {@link FlowCallback}.
	 */
	public boolean isFlowCallback() {
		return this.isFlowCallback;
	}

}