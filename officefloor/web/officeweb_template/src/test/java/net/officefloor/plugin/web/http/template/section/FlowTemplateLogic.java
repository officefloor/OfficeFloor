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
package net.officefloor.plugin.web.http.template.section;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;

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

	public void getTemplate(FlowsToAllTasks flows, ServerHttpConnection connection) throws Exception {
		this.doFlow("getTemplate", flows, connection);
	}

	public FlowTemplateLogic getOne(FlowsToAllTasks flows, ServerHttpConnection connection) throws Exception {
		this.doFlow("getOne", flows, connection);
		return this;
	}

	public String getValue() {
		return "1";
	}

	public void getTwo(FlowsToAllTasks flows, ServerHttpConnection connection) throws Exception {
		this.doFlow("getTwo", flows, connection);
	}

	public void getEnd(FlowsToAllTasks flows, ServerHttpConnection connection) throws Exception {
		this.doFlow("getEnd", flows, connection);
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
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @throws Exception
	 *             If fails to do the flow.
	 */
	private void doFlow(String methodName, FlowsToAllTasks flows, ServerHttpConnection connection) throws Exception {

		// Determine if already invoked flow for method (stops infinite loop)
		if (this.invokedFlows.contains(methodName)) {
			return;
		}

		// Obtain the target flow
		HttpRequest request = connection.getHttpRequest();
		Map<String, String> parameters = HttpRequestTokeniserImpl.extractParameters(request);
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