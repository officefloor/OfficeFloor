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
package net.officefloor.plugin.socket.server.http.template.section;

import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Provides logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogic {

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
	 */
	public void submit(ServerHttpConnection connection, SubmitFlow flow) {
		// TODO something
	}

	/**
	 * Flows available for <code>submit</code>.
	 */
	@FlowInterface
	public static interface SubmitFlow {

		/**
		 * Does the flow.
		 * 
		 * @param parameter
		 *            Parameter.
		 */
		void doFlow(Integer parameter);
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