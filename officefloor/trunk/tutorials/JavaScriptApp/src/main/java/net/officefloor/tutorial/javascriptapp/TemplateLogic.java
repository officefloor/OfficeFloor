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
import net.officefloor.plugin.json.HttpJson;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.plugin.web.http.template.NotRenderTemplateAfter;

/**
 * Logic for the <code>template.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class AdditionRequest implements Serializable {
		private String numberOne;
		private String numberTwo;
	}

	@NotRenderTemplateAfter
	public void addition(AdditionRequest request,
			ServerHttpConnection connection) throws IOException {

		// Add the numbers
		int result = Integer.parseInt(request.getNumberOne())
				+ Integer.parseInt(request.getNumberTwo());

		// Return the result
		connection.getHttpResponse().getEntityWriter()
				.write(String.valueOf(result));
	}

	@Data
	@HttpJson
	public static class SubtractionRequest implements Serializable {
		private String numberOne;
		private String numberTwo;
	}

	@Data
	public static class JsonResponse {
		private final String result;
	}

	public void subtraction(SubtractionRequest request,
			JsonResponseWriter response) throws IOException {

		// Subtract the numbers
		int result = Integer.parseInt(request.getNumberOne())
				- Integer.parseInt(request.getNumberTwo());

		// Return the result
		response.writeResponse(new JsonResponse(String.valueOf(result)));
	}

}