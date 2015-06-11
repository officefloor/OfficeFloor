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
package net.officefloor.plugin.json.write;

import java.io.IOException;
import java.io.Writer;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link ManagedObjectSource} for the {@link JsonResponseWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonResponseWriterManagedObjectSource
		extends
		AbstractManagedObjectSource<JsonResponseWriterManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link ObjectMapper}.
	 */
	private ObjectMapper mapper;

	/*
	 * ===================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {

		// Provide the meta-data
		context.setObjectClass(JsonResponseWriter.class);
		context.setManagedObjectClass(JsonResponseWriterManagedObject.class);
		context.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context)
			throws Exception {
		// Create the object mapper
		this.mapper = new ObjectMapper();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JsonResponseWriterManagedObject();
	}

	/**
	 * {@link ManagedObject} for the {@link JsonResponseWriter}.
	 */
	private class JsonResponseWriterManagedObject implements
			CoordinatingManagedObject<Dependencies>, JsonResponseWriter {

		/**
		 * {@link ServerHttpConnection}.
		 */
		private ServerHttpConnection connection;

		/*
		 * =================== ManagedObject ========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {
			// Obtain the server HTTP connection
			this.connection = (ServerHttpConnection) registry
					.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * =================== JsonResponseWriter ====================
		 */

		@Override
		public void writeResponse(Object jsonObject) throws IOException {

			// Obtain the response
			HttpResponse response = this.connection.getHttpResponse();

			// Specify JSON as Content-Type
			response.setContentType("application/json", null);

			// Send the JSON response
			Writer responseWriter = response.getEntityWriter();
			JsonResponseWriterManagedObjectSource.this.mapper.writeValue(
					responseWriter, jsonObject);
		}
	}

}