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
package net.officefloor.tutorial.javascriptapp;

import java.io.IOException;
import java.io.Serializable;

import lombok.Data;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.template.NotRenderTemplateAfter;

/**
 * Logic for the <code>template.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: HttpParameters
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class AdditionRequest implements Serializable {
		private String numberOne;
		private String numberTwo;
	}

	@NotRenderTemplateAfter
	public void addition(AdditionRequest request, ServerHttpConnection connection) throws IOException {

		// Add the numbers
		int result = Integer.parseInt(request.getNumberOne()) + Integer.parseInt(request.getNumberTwo());

		// Return the result
		connection.getResponse().getEntityWriter().write(String.valueOf(result));
	}
	// END SNIPPET: HttpParameters

	// START SNIPPET: HttpJson
	@Data
	public static class SubtractionRequest implements Serializable {
		private String numberOne;
		private String numberTwo;
	}

	@Data
	public static class JsonResponse {
		private final String result;
	}

	public void subtraction(SubtractionRequest request, ObjectResponse<JsonResponse> response) throws IOException {

		// Subtract the numbers
		int result = Integer.parseInt(request.getNumberOne()) - Integer.parseInt(request.getNumberTwo());

		// Return the result
		response.send(new JsonResponse(String.valueOf(result)));
	}

}
// END SNIPPET: HttpJson