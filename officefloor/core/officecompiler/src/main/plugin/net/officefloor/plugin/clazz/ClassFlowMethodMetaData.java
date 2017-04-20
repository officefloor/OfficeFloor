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
package net.officefloor.plugin.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;

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
	 * Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	private final int flowIndex;

	/**
	 * Flag indicating if there is a parameter for the {@link Flow}.
	 */
	private final boolean isParameter;

	/**
	 * Flag indicating if {@link FlowCallback} for the {@link Flow}.
	 */
	private final boolean isFlowCallback;

	/**
	 * Initiate.
	 * 
	 * @param flowType
	 *            Type declaring the {@link Method} of this flow.
	 * @param method
	 *            {@link Method}.
	 * @param flowIndex
	 *            Index of the {@link Flow} to invoke for this {@link Method}.
	 * @param isParameter
	 *            <code>true</code> if a parameter for the {@link Flow}.
	 * @param isFlowCallback
	 *            <code>true</code> if last parameter is {@link FlowCallback}.
	 */
	public ClassFlowMethodMetaData(Class<?> flowType, Method method, int flowIndex, boolean isParameter,
			boolean isFlowCallback) {
		this.flowType = flowType;
		this.method = method;
		this.flowIndex = flowIndex;
		this.isParameter = isParameter;
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
	 * Obtains the index of the {@link Flow} to invoke for this {@link Method}.
	 * 
	 * @return Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	public int getFlowIndex() {
		return this.flowIndex;
	}

	/**
	 * Flags if a parameter for the {@link Flow}.
	 * 
	 * @return <code>true</code> if a parameter for the {@link Flow}.
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