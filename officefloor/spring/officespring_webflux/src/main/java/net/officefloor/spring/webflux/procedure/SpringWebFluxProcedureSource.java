/*-
 * #%L
 * Spring Web Flux Integration
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

package net.officefloor.spring.webflux.procedure;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
import net.officefloor.web.state.HttpRequestState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Web Flux {@link Controller} {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * {@link SpringWebFluxProcedureSource} source name.
	 */
	public static final String SOURCE_NAME = "SpringWebFlux" + Controller.class.getSimpleName();

	/*
	 * ====================== ProcedureSourceServiceFactory ======================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ManagedFunctionProcedureSource =====================
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

		// Ensure controller class
		Controller controller = AnnotatedElementUtils.findMergedAnnotation(controllerClass, Controller.class);
		if (controller == null) {
			return; // not controller
		}

		// Load all the request mapped methods
		NEXT_METHOD: for (Method method : controllerClass.getMethods()) {

			// Ensure Web Flux method
			Class<?> returnType = method.getReturnType();
			if ((returnType == null)
					|| ((!Mono.class.isAssignableFrom(returnType)) && (!Flux.class.isAssignableFrom(returnType)))) {
				continue NEXT_METHOD;
			}

			// Determine if request mapped method
			RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
			if (requestMapping == null) {
				continue NEXT_METHOD;
			}

			// Add the method
			context.addProcedure(method.getName());
		}
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		SourceContext sourceContext = context.getSourceContext();

		// Obtain the Controller class and method
		Class<?> controllerClass = sourceContext.loadClass(context.getResource());
		String methodName = context.getProcedureName();

		// Create the procedure
		SpringWebFluxProcedure procedure = new SpringWebFluxProcedure(controllerClass, methodName);

		// Determine if register the procedure
		if (!sourceContext.isLoadingType()) {
			SpringWebFluxProcedureRegistry.registerSpringControllerProcedure(procedure);
		}

		// Provide managed function
		ManagedFunctionTypeBuilder<SpringWebFluxProcedure.DependencyKeys, None> servlet = context
				.setManagedFunction(procedure, SpringWebFluxProcedure.DependencyKeys.class, None.class);
		servlet.addObject(ServerHttpConnection.class)
				.setKey(SpringWebFluxProcedure.DependencyKeys.SERVER_HTTP_CONNECTION);
		servlet.addObject(HttpRequestState.class).setKey(SpringWebFluxProcedure.DependencyKeys.HTTP_REQUEST_STATE);
	}

}
