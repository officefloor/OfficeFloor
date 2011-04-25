/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import java.io.Serializable;
import java.io.Writer;

import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpSessionStateful;

/**
 * Provides stateful logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
@HttpSessionStateful
public class StatefulTemplateLogic implements Serializable {

	/**
	 * Counter.
	 */
	private int counter = 1;

	/**
	 * Obtains this as bean for Template section.
	 * 
	 * @return Bean for template section.
	 */
	public StatefulTemplateLogic getTemplate() {
		return this;
	}

	/**
	 * Obtains the count.
	 * 
	 * @return Count.
	 */
	public int getCount() {
		return this.counter;
	}

	/**
	 * Handles the increment link.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return Incremented count value.
	 * @throws IOException
	 *             If fails to indicate incremented.
	 */
	@NextTask("doExternalFlow")
	public String increment(ServerHttpConnection connection) throws IOException {

		// Indicate increment
		Writer writer = new OutputStreamWriter(connection.getHttpResponse()
				.getBody().getOutputStream());
		writer.write("increment");
		writer.flush();

		// Increment counter
		this.counter++;

		// Return value of counter
		return String.valueOf(this.counter);
	}

}