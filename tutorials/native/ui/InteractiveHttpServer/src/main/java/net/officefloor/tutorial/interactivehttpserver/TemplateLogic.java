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
		private static final long serialVersionUID = 1L;

		private String name;

		private Issue nameIssue;

		private String description;

		private String successMessage;
	}

	@Data
	public static class Issue implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String message;
	}

	/**
	 * Obtains the bean for rendering the template.
	 * 
	 * @param submittedParameters Same {@link Parameters} that was constructed for
	 *                            {@link #handleSubmission(Parameters)}. This allows
	 *                            the page to be rendered with the values provided
	 *                            by the client.
	 * @return {@link Parameters} for rendering to page.
	 */
	public Parameters getTemplateData(Parameters submittedParameters) {
		return submittedParameters;
	}

	/**
	 * Reflectively invoked to handle form submission.
	 * 
	 * @param submittedParameters {@link Parameters} which is dependency injected.
	 *                            It is constructed via its default constructor and
	 *                            has the HTTP parameters values loaded by
	 *                            corresponding names.
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