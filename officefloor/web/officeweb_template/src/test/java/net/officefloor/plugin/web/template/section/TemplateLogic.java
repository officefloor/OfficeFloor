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
package net.officefloor.plugin.web.template.section;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.Property;
import net.officefloor.plugin.web.template.NotRenderTemplateAfter;
import net.officefloor.plugin.web.template.NotEscaped;
import net.officefloor.plugin.web.template.parse.HttpTemplate;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.tokenise.HttpRequestTokeniserImpl;

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
	@ManagedObject(source = ClassManagedObjectSource.class, properties = @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = RowBean.class))
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
	 * @param session
	 *            {@link HttpSession}.
	 * @return {@link RowBean} instances.
	 */
	public RowBean[] getList(HttpSession session) {
		return new RowBean[] { new RowBean("row", "test row") };
	}

	/**
	 * Handles the nextTask link.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return Parameter for the next task.
	 * @throws IOException
	 *             Escalation.
	 */
	@NextFunction("doExternalFlow")
	public String nextTask(ServerHttpConnection connection) throws IOException {

		// Indicate next task
		connection.getHttpResponse().getEntityWriter().write("nextTask");

		// Return parameter
		return "NextTask";
	}

	/**
	 * Handles the submit link.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param flow
	 *            {@link SubmitFlow}.
	 * @throws SQLException
	 *             Escalation.
	 * @throws IOException
	 *             Escalation.
	 */
	public void submit(ServerHttpConnection connection, SubmitFlow flow) throws SQLException, IOException {

		// Indicate submit
		connection.getHttpResponse().getEntityWriter().write("<submit />");

		// Obtain whether to invoke flow
		String doFlowValue = HttpRequestTokeniserImpl.extractParameters(connection.getHttpRequest()).get("doFlow");
		if ("true".equals(doFlowValue)) {
			// Trigger flow
			flow.doInternalFlow(new Integer(1));
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
		 * @param parameter
		 *            Parameter.
		 */
		void doInternalFlow(Integer parameter);

		/**
		 * Does the external flow.
		 * 
		 * @param parameter
		 *            Parameter.
		 */
		void doExternalFlow(String parameter);
	}

	/**
	 * Handles internal flow from {@link SubmitFlow}.
	 * 
	 * @param parameter
	 *            Parameter.
	 * @param sqlConnection
	 *            {@link Connection}.
	 * @param httpConnection
	 *            {@link ServerHttpConnection}.
	 * @return Parameter for external flow.
	 * @throws IOException
	 *             Escalation.
	 */
	@NextFunction("doExternalFlow")
	public String doInternalFlow(@Parameter Integer parameter, Connection sqlConnection,
			ServerHttpConnection httpConnection) throws IOException {

		// Indicate internal flow with its parameter
		Writer writer = httpConnection.getHttpResponse().getEntityWriter();
		writer.write(" - doInternalFlow[");
		writer.write(String.valueOf(parameter.intValue()));
		writer.write("]");

		// Return parameter for next flow
		return "Parameter for External Flow";
	}

	/**
	 * Flags to not render the {@link HttpTemplate} afterwards.
	 */
	@NotRenderTemplateAfter
	public void notRenderTemplateAfter(ServerHttpConnection connection) throws IOException {
		connection.getHttpResponse().getEntityWriter().write("NOT_RENDER_TEMPLATE_AFTER");
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
		 * @param count
		 *            Count.
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
		public RowBean() {
			this("name", "description");
		}

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param description
		 *            Description.
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