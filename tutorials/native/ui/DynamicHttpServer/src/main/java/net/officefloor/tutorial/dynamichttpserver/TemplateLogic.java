package net.officefloor.tutorial.dynamichttpserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	@Data
	public static class Values {

		private final String time;

		private final Property[] properties;

		private final Object noBean;
	}

	@Data
	public class Property {

		private final String name;

		private final String value;
	}

	/**
	 * Reflectively invoked by WoOF to obtain the dynamic values to render to
	 * the page.
	 * 
	 * @return Bean containing the dynamic values to render to the page
	 *         identified by bean property name.
	 */
	public Values getTemplateData() {

		// Obtain the time
		String time = SimpleDateFormat.getTimeInstance().format(new Date());

		// Create the listing of properties
		List<Property> properties = new LinkedList<Property>();
		for (String name : System.getProperties().stringPropertyNames()) {
			String value = System.getProperty(name);
			properties.add(new Property(name, value));
		}

		// Return the populated values
		return new Values(time, properties.toArray(new Property[properties
				.size()]), null);
	}

}
// END SNIPPET: example