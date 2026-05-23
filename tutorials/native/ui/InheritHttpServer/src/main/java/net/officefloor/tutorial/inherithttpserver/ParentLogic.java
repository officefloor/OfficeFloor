package net.officefloor.tutorial.inherithttpserver;

import lombok.Data;

/**
 * Logic for the <code>parent.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ParentLogic {

	@Data
	public static class HeaderValues {
		private final String text;
	}

	public HeaderValues getTemplateData() {
		return new HeaderValues("HEADER");
	}
}
// END SNIPPET: tutorial