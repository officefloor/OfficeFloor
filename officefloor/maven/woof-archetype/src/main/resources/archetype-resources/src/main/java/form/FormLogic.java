package ${package}.form;

import java.io.Serializable;

import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.web.HttpParameters;

/**
 * Logic for the <code>form.woof.html</code>.
 */
public class FormLogic {

	@HttpParameters
	public static class Parameters implements Serializable {

		private String name;

		private Issue nameIssue;

		private String age;

		private Issue ageIssue;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Issue getNameIssue() {
			return nameIssue;
		}

		public void setNameIssue(Issue nameIssue) {
			this.nameIssue = nameIssue;
		}

		public String getAge() {
			return age;
		}

		public void setAge(String age) {
			this.age = age;
		}

		public Issue getAgeIssue() {
			return ageIssue;
		}

		public void setAgeIssue(Issue ageIssue) {
			this.ageIssue = ageIssue;
		}
	}

	public static class Issue {

		private String message;

		public Issue(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}

	@FlowInterface
	public static interface Flows {

		void complete();
	}

	/**
	 * Provides the {@link Parameters} for values to template rendering.
	 * 
	 * @param parameters
	 *            Either new {@link Parameters} or the {@link Parameters} from
	 *            form submission.
	 * @return {@link Parameters} for template values.
	 */
	public Parameters getTemplate(Parameters parameters) {
		return parameters;
	}

	/**
	 * Processes the submission of the form.
	 * 
	 * @param parameters
	 *            {@link Parameters} loaded with HTTP parameters values from
	 *            form submission.
	 * @param flows
	 *            {@link Flows} to control flow of navigation.
	 */
	public void processValues(Parameters parameters, Flows flows) {

		boolean isIssue = false;

		// Must have name
		String name = parameters.getName();
		if ((name == null) || (name.trim().length() == 0)) {
			parameters.setNameIssue(new Issue("Must provide name"));
			isIssue = true;
		}

		// Must have valid age
		String age = parameters.getAge();
		if ((age == null) || (age.trim().length() == 0)) {
			parameters.setAgeIssue(new Issue("Must provide age"));
			isIssue = true;
		} else {
			// Ensure number
			try {
				Integer.valueOf(age.trim());
			} catch (NumberFormatException ex) {
				parameters.setAgeIssue(new Issue("Age must be a number"));
				isIssue = true;
			}
		}

		// On issue, render page with details
		if (isIssue) {
			return;
		}

		/*
		 * TODO do some business logic with the values. May include further
		 * dependencies to this method such as a database connection for the
		 * business logic. See http://officefloor.net/tutorials for more
		 * details.
		 */

		// Navigate away from page
		flows.complete();
	}

}