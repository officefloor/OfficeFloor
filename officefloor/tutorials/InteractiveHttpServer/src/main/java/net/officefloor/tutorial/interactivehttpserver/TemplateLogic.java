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
package net.officefloor.tutorial.interactivehttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.web.HttpParameters;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: class
public class TemplateLogic {

	@Data
	@HttpParameters
	public static class Parameters implements Serializable {

		private String name;

		private Issue nameIssue;

		private String description;

		private String successMessage;
	}

	@Data
	public static class Issue implements Serializable {

		private final String message;
	}

	/**
	 * Obtains the bean for rendering the template.
	 * 
	 * @param submittedParameters
	 *            Same {@link Parameters} that was constructed for
	 *            {@link #handleSubmission(Parameters)}. This allows the page to be
	 *            rendered with the values provided by the client.
	 * @return {@link Parameters} for rendering to page.
	 */
	public Parameters getTemplateData(Parameters submittedParameters) {
		return submittedParameters;
	}

	/**
	 * Reflectively invoked to handle form submission.
	 * 
	 * @param submittedParameters
	 *            {@link Parameters} which is dependency injected. It is constructed
	 *            via its default constructor and has the HTTP parameters values
	 *            loaded by corresponding names.
	 */
	public void handleSubmission(Parameters submittedParameters) {

		// Ensure have a name provided
		String name = submittedParameters.getName();
		if ((name == null) || (name.trim().length() == 0)) {
			submittedParameters.setNameIssue(new Issue("Must provide name"));
			return;
		}

		// TODO use values to undertake some business logic. Typically would
		// provider further dependencies as parameters to this method to allow
		// this.

		// Provide success message (and clear values)
		submittedParameters.setSuccessMessage("Thank you " + name);
		submittedParameters.setName(null);
		submittedParameters.setDescription(null);
	}
}
// END SNIPPET: class