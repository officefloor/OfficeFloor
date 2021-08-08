/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.section;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.PropertyValue;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.template.NotEscaped;
import net.officefloor.web.template.NotRenderTemplateAfter;
import net.officefloor.web.template.parse.ParsedTemplate;

/**
 * Provides logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogic {

	/**
	 * Dependency injected object.
	 */
	@Dependency
	Connection connection;

	/**
	 * {@link ManagedObject} injection.
	 */
	@ManagedObject(source = ClassManagedObjectSource.class, properties = @PropertyValue(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = RowBean.class))
	RowBean managedObject;

	/**
	 * Obtains the bean for starting template.
	 * 
	 * @return Starting template bean.
	 */
	public TemplateLogic getTemplate() {
		return this;
	}

	/**
	 * Obtains the template name.
	 * 
	 * @return Template name.
	 */
	public String getTemplateName() {
		return "Test";
	}

	/**
	 * Obtains the HTML to be escaped.
	 * 
	 * @return HTML to be escaped.
	 */
	public String getEscapedHtml() {
		return this.getUnescapedHtml();
	}

	/**
	 * Obtains the HTML to be rendered as is.
	 * 
	 * @return HTML to be rendered as is.
	 */
	@NotEscaped
	public String getUnescapedHtml() {
		return "<img src=\"Test.png\" />";
	}

	/**
	 * Obtains <code>null</code> value for bean to not render.
	 * 
	 * @return <code>null</code>.
	 */
	public Object getNullBean() {
		return null;
	}

	/**
	 * Obtains the bean to render.
	 * 
	 * @return Bean to render.
	 */
	public TemplateLogic getBean() {
		return this;
	}

	/**
	 * Obtains the bean property.
	 * 
	 * @return Bean property.
	 */
	public String getBeanProperty() {
		return "bean-property";
	}

	/**
	 * Obtains the bean array.
	 * 
	 * @return Bean array.
	 */
	public ArrayBean[] getBeanArray() {
		ArrayBean[] beans = new ArrayBean[10];
		for (int i = 0; i < beans.length; i++) {
			beans[i] = new ArrayBean(i);
		}
		return beans;
	}

	/**
	 * Obtains the {@link RowBean} instances.
	 * 
	 * @param session {@link HttpSession}.
	 * @return {@link RowBean} instances.
	 */
	public RowBean[] getList(HttpSession session) {
		return new RowBean[] { new RowBean("row", "test row") };
	}

	/**
	 * Handles the nextFunction link.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 * @return Parameter for the next task.
	 * @throws IOException Escalation.
	 */
	@Next("doExternalFlow")
	public String nextFunction(ServerHttpConnection connection) throws IOException {

		// Indicate next task
		connection.getResponse().getEntityWriter().write("nextTask");

		// Return parameter
		return "NextFunction";
	}

	/**
	 * Handles the submit link.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 * @param flow       {@link SubmitFlow}.
	 * @throws SQLException Escalation.
	 * @throws IOException  Escalation.
	 */
	public void submit(@HttpQueryParameter("doFlow") String doFlow, ServerHttpConnection connection, SubmitFlow flow)
			throws SQLException, IOException {

		// Indicate submit
		connection.getResponse().getEntityWriter().write("<submit />");

		// Obtain whether to invoke flow
		if ("true".equals(doFlow)) {
			// Trigger flow
			flow.doInternalFlow(Integer.valueOf(1));
		}
	}

	/**
	 * Flows available for <code>submit</code>.
	 */
	@FlowInterface
	public static interface SubmitFlow {

		/**
		 * Does the internal flow.
		 * 
		 * @param parameter Parameter.
		 */
		void doInternalFlow(Integer parameter);

		/**
		 * Does the external flow.
		 * 
		 * @param parameter Parameter.
		 */
		void doExternalFlow(String parameter);
	}

	/**
	 * Handles internal flow from {@link SubmitFlow}.
	 * 
	 * @param parameter      Parameter.
	 * @param sqlConnection  {@link Connection}.
	 * @param httpConnection {@link ServerHttpConnection}.
	 * @return Parameter for external flow.
	 * @throws IOException Escalation.
	 */
	@Next("doExternalFlow")
	public String doInternalFlow(@Parameter Integer parameter, Connection sqlConnection,
			ServerHttpConnection httpConnection) throws IOException {

		// Indicate internal flow with its parameter
		Writer writer = httpConnection.getResponse().getEntityWriter();
		writer.write(" - doInternalFlow[");
		writer.write(String.valueOf(parameter.intValue()));
		writer.write("]");

		// Return parameter for next flow
		return "Parameter for External Flow";
	}

	/**
	 * Flags to not render the {@link ParsedTemplate} afterwards.
	 */
	@NotRenderTemplateAfter
	public void notRenderTemplateAfter(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("NOT_RENDER_TEMPLATE_AFTER");
	}

	/**
	 * Array bean.
	 */
	public static class ArrayBean {

		/**
		 * Count.
		 */
		private int count;

		/**
		 * Initiate.
		 * 
		 * @param count Count.
		 */
		public ArrayBean(int count) {
			this.count = count;
		}

		/**
		 * Obtains the count.
		 * 
		 * @return Count.
		 */
		public int getCount() {
			return this.count;
		}
	}

	/**
	 * Row of table bean.
	 */
	public static class RowBean {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Description.
		 */
		private final String description;

		/**
		 * Initiate.
		 */
		@Dependency
		public RowBean() {
			this("name", "description");
		}

		/**
		 * Initiate.
		 * 
		 * @param name        Name.
		 * @param description Description.
		 */
		public RowBean(String name, String description) {
			this.name = name;
			this.description = description;
		}

		/**
		 * Obtains the name.
		 * 
		 * @return Name.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Obtains the description.
		 * 
		 * @return Description.
		 */
		public String getDescription() {
			return this.description;
		}
	}

}
