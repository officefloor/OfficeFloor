/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs.procedure;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

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
