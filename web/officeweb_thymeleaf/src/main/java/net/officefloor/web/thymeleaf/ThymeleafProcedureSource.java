/*-
 * #%L
 * Web Thymeleaf
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.web.thymeleaf;

import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * {@link ProcedureSource} that renders a Thymeleaf template.
 *
 * <p>The resource name is the Thymeleaf template name (without prefix/suffix).
 * The procedure parameter (passed from the previous composition step) is
 * exposed to the template as the {@code model} variable.
 */
public class ThymeleafProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/** Source name. */
	public static final String SOURCE_NAME = "Thymeleaf";
	/** Procedure name. */
	public static final String PROCEDURE_NAME = "render";

	/*
	 * ==================== ProcedureSourceServiceFactory =====================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================== ProcedureSource =============================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// Only offer render for non-class resources (i.e. template names)
		if (context.getSourceContext().loadOptionalClass(context.getResource()) == null) {
			context.addProcedure(PROCEDURE_NAME);
		}
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		String templateName = context.getResource();

		ManagedFunctionTypeBuilder<Indexed, None> builder = context.setManagedFunction(
				new ThymeleafRenderFunction(templateName), Indexed.class, None.class);

		builder.addObject(TemplateEngine.class).setLabel("TEMPLATE_ENGINE");
		builder.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		builder.addObject(Object.class).setLabel("MODEL").addAnnotation(new ParameterAnnotation());
		builder.addEscalation(IOException.class);
	}

	/**
	 * {@link StaticManagedFunction} that renders a Thymeleaf template.
	 */
	private static class ThymeleafRenderFunction extends StaticManagedFunction<Indexed, None> {

		private final String templateName;

		ThymeleafRenderFunction(String templateName) {
			this.templateName = templateName;
		}

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws IOException {
			TemplateEngine engine = (TemplateEngine) context.getObject(0);
			ServerHttpConnection connection = (ServerHttpConnection) context.getObject(1);
			Object model = context.getObject(2);

			Context ctx = new Context();
			if (model != null) {
				ctx.setVariable("model", model);
			}

			HttpResponse response = connection.getResponse();
			response.setContentType("text/html", StandardCharsets.UTF_8);
			engine.process(this.templateName, ctx, response.getEntityWriter());
		}
	}

}
