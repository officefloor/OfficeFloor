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
package net.officefloor.tutorial.testhttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.plugin.web.http.application.HttpParameters;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class Parameters implements Serializable {

		private String a;

		private String b;

		private String result;
	}

	public Parameters getTemplateData(Parameters parameters) {
		return parameters;
	}

	public void add(Parameters parameters) {
		
		int a = Integer.parseInt(parameters.getA());
		int b = Integer.parseInt(parameters.getB());

		parameters.setResult(String.valueOf(a + b));
	}

}
// END SNIPPET: tutorial