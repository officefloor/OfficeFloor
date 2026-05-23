package net.officefloor.tutorial.inherithttpserver;

import lombok.Data;
import net.officefloor.web.state.HttpRequestState;

/**
 * Logic for the <code>grandchild.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class GrandChildLogic extends ParentLogic {

	@Data
	public static class AlternateHeaderValues {
		private final String other;
	}

	public AlternateHeaderValues getTemplateData(HttpRequestState requestState) {
		return new AlternateHeaderValues("GRAND CHILD");
	}
}
// END SNIPPET: tutorial