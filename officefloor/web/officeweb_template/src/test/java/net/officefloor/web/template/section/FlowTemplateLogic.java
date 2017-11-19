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
package net.officefloor.web.template.section;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.web.state.HttpRequestState;

/**
 * Provides logic for the flow template.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowTemplateLogic {

	@FlowInterface
	public static interface FlowsToAllTasks {

		void getTemplate();

		void template();

		void getOne();

		void one();

		void getTwo();

		void two();

		void getEnd();

		void end();
	}

	public void getTemplate(FlowsToAllTasks flows, HttpRequestState requestState) throws Exception {
		this.doFlow("getTemplate", flows, requestState);
	}

	public FlowTemplateLogic getOne(FlowsToAllTasks flows, HttpRequestState requestState) throws Exception {
		this.doFlow("getOne", flows, requestState);
		return this;
	}

	public String getValue() {
		return "1";
	}

	public void getTwo(FlowsToAllTasks flows, HttpRequestState requestState) throws Exception {
		this.doFlow("getTwo", flows, requestState);
	}

	public void getEnd(FlowsToAllTasks flows, HttpRequestState requestState) throws Exception {
		this.doFlow("getEnd", flows, requestState);
	}

	@NextFunction("doExternalFlow")
	public void link() {
	}

	/**
	 * Cleared each request to only contain flow invocations for the request.
	 */
	private Set<String> invokedFlows = new HashSet<String>();

	/**
	 * Does the flow.
	 * 
	 * @param methodName
	 *            Name of method be run.
	 * @param flows
	 *            {@link FlowsToAllTasks}.
	 * @param requestState
	 *            {@link HttpRequestState}.
	 * @throws Exception
	 *             If fails to do the flow.
	 */
	private void doFlow(String methodName, FlowsToAllTasks flows, HttpRequestState requestState) throws Exception {

		// Determine if already invoked flow for method (stops infinite loop)
		if (this.invokedFlows.contains(methodName)) {
			return;
		}

		// Obtain the target flow
		Map<String, String> parameters = new HashMap<>();
		requestState.loadValues((name, value, location) -> parameters.put(name, value));
		String targetFlow = parameters.get(methodName);

		// Do nothing if no configured flow
		if (targetFlow == null) {
			return;
		}

		// Add flow invocation
		this.invokedFlows.add(methodName);

		// Direct to flow
		Method flow = flows.getClass().getMethod(targetFlow);
		flow.invoke(flows);
	}

}