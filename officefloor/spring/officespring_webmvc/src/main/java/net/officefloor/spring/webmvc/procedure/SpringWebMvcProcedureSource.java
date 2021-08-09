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

import org.springframework.stereotype.Controller;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.web.state.HttpRequestState;

/**
 * Web MVC {@link Controller} {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebMvcProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * {@link SpringWebMvcProcedureSource} source name.
	 */
	public static final String SOURCE_NAME = "SpringWebMvc" + Controller.class.getSimpleName();

	/*
	 * ===================== ProcedureSourceServiceFactory ========================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ ProcedureSource ===============================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {

		// Attempt to load class
		Class<?> controllerClass = context.getSourceContext().loadOptionalClass(context.getResource());
		if (controllerClass == null) {
			return;
		}

		// Load the end point methods
		SpringWebMvcProcedureRegistry.extractEndPointMethods(controllerClass,
				(method) -> context.addProcedure(method.getName()));
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		SourceContext sourceContext = context.getSourceContext();

		// Obtain the Controller class and method
		Class<?> controllerClass = sourceContext.loadClass(context.getResource());
		String methodName = context.getProcedureName();

		// Determine if loading type
		ServletServicer servletServicer = null;
		if (!sourceContext.isLoadingType()) {

			// Obtain the Servlet Manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Add the dispatcher Servlet
			String servletName = sourceContext.getName();
			servletServicer = servletManager.addServlet(servletName, ProcedureDispatcherServlet.class, null);
		}

		// Create the procedure
		SpringWebMvcProcedure procedure = new SpringWebMvcProcedure(servletServicer, controllerClass, methodName);

		// Determine if register the procedure
		if (!sourceContext.isLoadingType()) {
			SpringWebMvcProcedureRegistry.registerSpringControllerProcedure(procedure);
		}

		// Provide managed function
		ManagedFunctionTypeBuilder<SpringWebMvcProcedure.DependencyKeys, None> servlet = context
				.setManagedFunction(procedure, SpringWebMvcProcedure.DependencyKeys.class, None.class);
		servlet.addObject(ServerHttpConnection.class)
				.setKey(SpringWebMvcProcedure.DependencyKeys.SERVER_HTTP_CONNECTION);
		servlet.addObject(HttpRequestState.class).setKey(SpringWebMvcProcedure.DependencyKeys.HTTP_REQUEST_STATE);

		// Must depend on following for thread locals to be available
		servlet.addObject(ServletServicer.class).setKey(SpringWebMvcProcedure.DependencyKeys.SERVLET_SERVICER);
	}

}
