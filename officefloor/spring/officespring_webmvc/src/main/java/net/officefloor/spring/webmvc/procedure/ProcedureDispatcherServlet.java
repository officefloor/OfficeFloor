/*-
 * #%L
 * Spring Web MVC Integration
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

package net.officefloor.spring.webmvc.procedure;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;

import net.officefloor.plugin.clazz.Dependency;

/**
 * {@link DispatcherServlet} for the {@link SpringWebMvcProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureDispatcherServlet extends DispatcherServlet {

	/**
	 * Attribute name for the {@link HandlerExecutionChain}.
	 */
	public static final String ATTRIBUTE_HANDLER_EXECUTION_CHAIN = "officefloor.procedure.handler";

	/**
	 * Serialise version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link WebApplicationContext}.
	 */
	@Dependency
	private WebApplicationContext injectedWebApplicationContext;

	/*
	 * ==================== DispatcherServlet =========================
	 */

	@Override
	protected WebApplicationContext initWebApplicationContext() {

		// Refresh based on context
		this.onRefresh(this.injectedWebApplicationContext);

		// Return the context
		return this.injectedWebApplicationContext;
	}

	@Override
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		return (HandlerExecutionChain) request.getAttribute(ATTRIBUTE_HANDLER_EXECUTION_CHAIN);
	}

}
