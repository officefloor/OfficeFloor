package net.officefloor.tutorial.activityhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;

public class IncreaseDepthLogic {

	// START SNIPPET: tutorial
	public Depth increase(@Parameter Depth depth) {
		return new Depth(depth.getLevel() + 1);
	}
	// END SNIPPET: tutorial
}
