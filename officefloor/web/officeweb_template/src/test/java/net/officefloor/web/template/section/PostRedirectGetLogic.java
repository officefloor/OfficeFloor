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
package net.officefloor.web.template.section;

import java.io.IOException;
import java.io.Serializable;

import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;

/**
 * Logic for the POST/Redirect/GET pattern tests.
 * 
 * @author Daniel Sagenschneider
 */
public class PostRedirectGetLogic {

	/**
	 * Parameters.
	 */
	public static class Parameters implements Serializable {

		private String text;

		private String operation;

		public String getText() {
			return this.text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}
	}

	/**
	 * Handles the <code>POST</code>.
	 * 
	 * @param parmeters
	 *            {@link Parameters}.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @throws IOException
	 *             If fails to write content to the {@link HttpResponse}.
	 */
	public void post(Parameters parameters, ServerHttpConnection connection) throws IOException {

		// Obtain the response
		HttpResponse response = connection.getResponse();

		// Determine if provide header
		if ("HEADER".equals(parameters.operation)) {
			response.getHeaders().addHeader("NAME", "VALUE");

		} else if ("ENTITY".equals(parameters.operation)) {
			ServerWriter writer = response.getEntityWriter();
			writer.write("entity");

		} else if ("REQUEST_STATE".equals(parameters.operation)) {
			parameters.setText("RequestState");
		}
	}

	/**
	 * Obtains the data for the template.
	 * 
	 * @param parameters
	 *            {@link Parameters}.
	 * @return Data for the template.
	 */
	public Parameters getTemplateData(Parameters parameters) {
		return parameters;
	}

	/**
	 * Required for setup.
	 */
	@NextFunction("doExternalFlow")
	public void requiredForSetup() {
	}

}