package net.officefloor.tutorial.inherithttpserver;

import lombok.Data;

/**
 * Logic for <code>child.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ChildLogic extends ParentLogic {

	@Data
	public static class IntroducedValues {
		private final String value;
	}

	public IntroducedValues getIntroducedData() {
		return new IntroducedValues("CHILD");
	}
}
// END SNIPPET: tutorial