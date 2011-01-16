/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.clazz.Property;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.work.clazz.FlowInterface;

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
	 * Handles the submit.
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
	public void submit(ServerHttpConnection connection, SubmitFlow flow)
			throws SQLException, IOException {

		// Indicate submit
		Writer writer = new OutputStreamWriter(connection.getHttpResponse()
				.getBody().getOutputStream());
		writer.write("submit");
		writer.flush();

		// Trigger flow
		flow.doInternalFlow(new Integer(1));
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
	@NextTask("doExternalFlow")
	public String doInternalFlow(@Parameter Integer parameter,
			Connection sqlConnection, ServerHttpConnection httpConnection)
			throws IOException {

		// Indicate internal flow with its parameter
		Writer writer = new OutputStreamWriter(httpConnection.getHttpResponse()
				.getBody().getOutputStream());
		writer.write(" - doInternalFlow[");
		writer.write(parameter.intValue());
		writer.write("]");
		writer.flush();

		// Return parameter for next flow
		return "Parameter for External Flow";
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