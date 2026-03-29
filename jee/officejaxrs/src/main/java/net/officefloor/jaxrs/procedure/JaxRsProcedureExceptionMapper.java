/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs.procedure;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link ExceptionMapper} for the {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsProcedureExceptionMapper implements ExceptionMapper<Throwable> {

	/**
	 * {@link HttpServletRequest}.
	 */
	private @Inject HttpServletRequest request;

	/**
	 * {@link HttpServletResponse}.
	 */
	private @Inject HttpServletResponse response;

	/*
	 * ===================== ExceptionMapper ==========================
	 */

	@Override
	public Response toResponse(Throwable exception) {

		// Send the error
		try {
			ServletSupplierSource.sendError(exception, this.request, this.response);
		} catch (IOException ex) {
			// Should not occur. However, just carry on to provide response
		}

		// Provide response as fallback to sending error
		return Response.serverError().entity(new OfficeFloorJaxRsError(exception.getMessage())).build();
	}

	/**
	 * Error entity for JAX-RS failure.
	 */
	public static class OfficeFloorJaxRsError {

		/**
		 * Error.
		 */
		private final String error;

		/**
		 * Instantiate.
		 * 
		 * @param error Error.
		 */
		public OfficeFloorJaxRsError(String error) {
			this.error = error;
		}

		/**
		 * Obtains the error.
		 * 
		 * @return Error.
		 */
		public String getError() {
			return this.error;
		}
	}

}
