package net.officefloor.tutorial.sectionhttpserver;

import lombok.Data;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	@Data
	public static class Values {

		private final String text;
	}

	@FlowInterface
	public static interface Flows {

		void noBean();
	}

	/**
	 * Obtains the data for the template section.
	 * 
	 * @return {@link Values}.
	 */
	public Values getTemplateData() {
		return new Values("Hi");
	}

	/**
	 * Obtains the data for the hello section.
	 * 
	 * @return {@link Values}.
	 */
	public Values getHelloData() {
		return new Values("Hello");
	}

	/**
	 * Skips not render section.
	 * 
	 * @param flows
	 *            {@link Flows} which allows rendering control over the sections
	 *            of the template. As this method is called before rendering the
	 *            section it skips rendering to the <code>noBean</code> section.
	 */
	public void getNotRender(Flows flows) {
		flows.noBean();
	}

}
// END SNIPPET: example