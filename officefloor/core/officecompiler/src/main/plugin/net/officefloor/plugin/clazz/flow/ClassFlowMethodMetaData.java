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

package net.officefloor.plugin.clazz.flow;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Meta-data of a {@link Method} on a {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassFlowMethodMetaData {

	/**
	 * {@link Method}.
	 */
	private final Method method;

	/**
	 * Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	private final int flowIndex;

	/**
	 * Flag indicating if parameter for the {@link Flow}.
	 */
	private final boolean isParameter;

	/**
	 * Flag indicating if {@link FlowCallback} for the {@link Flow}.
	 */
	private final boolean isFlowCallback;

	/**
	 * Initiate.
	 * 
	 * @param method         {@link Method}.
	 * @param flowIndex      Index of the {@link Flow} to invoke for this
	 *                       {@link Method}.
	 * @param isParameter    Flag indicating if parameter for the {@link Flow}.
	 * @param isFlowCallback <code>true</code> if last parameter is
	 *                       {@link FlowCallback}.
	 */
	public ClassFlowMethodMetaData(Method method, int flowIndex, boolean isParameter, boolean isFlowCallback) {
		this.method = method;
		this.flowIndex = flowIndex;
		this.isParameter = isParameter;
		this.isFlowCallback = isFlowCallback;
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
		return this.isParameter;
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
