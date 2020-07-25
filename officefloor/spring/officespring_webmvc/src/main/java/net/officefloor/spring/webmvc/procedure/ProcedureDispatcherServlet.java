/*-
 * #%L
 * Spring Web MVC Integration
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
