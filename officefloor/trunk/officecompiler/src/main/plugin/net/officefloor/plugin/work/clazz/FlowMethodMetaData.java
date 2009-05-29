/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Meta-data of a {@link Method} on a {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowMethodMetaData {

	/**
	 * Index of the {@link Flow} to invoke for this {@link Method}.
	 */
	private final int flowIndex;

	/**
	 * Flag indicating if there is a parameter for the {@link Flow}.
	 */
	private final boolean isParameter;

	/**
	 * Flag indicating if to return the {@link FlowFuture}.
	 */
	private final boolean isReturnFlowFuture;

	/**
	 * Initiate.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow} to invoke for this {@link Method}.
	 * @param isParameter
	 *            <code>true</code> if a parameter for the {@link Flow}.
	 * @param isReturnFlowFuture
	 *            <code>true</code> if to return the {@link FlowFuture}.
	 */
	public FlowMethodMetaData(int flowIndex, boolean isParameter,
			boolean isReturnFlowFuture) {
		this.flowIndex = flowIndex;
		this.isParameter = isParameter;
		this.isReturnFlowFuture = isReturnFlowFuture;
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
	 * Flags if to return the {@link FlowFuture}.
	 * 
	 * @return <code>true</code> if to return the {@link FlowFuture}.
	 */
	public boolean isReturnFlowFuture() {
		return this.isReturnFlowFuture;
	}

}