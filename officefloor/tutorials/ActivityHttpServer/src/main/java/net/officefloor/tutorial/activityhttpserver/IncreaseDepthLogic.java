package net.officefloor.tutorial.activityhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Logic to increase the depth.
 * 
 * @author Daniel Sagenschneider
 */
public class IncreaseDepthLogic {

	// START SNIPPET: tutorial
	public Depth increase(@Parameter Depth depth) {
		return new Depth(depth.getLevel() + 1);
	}
	// END SNIPPET: tutorial
}