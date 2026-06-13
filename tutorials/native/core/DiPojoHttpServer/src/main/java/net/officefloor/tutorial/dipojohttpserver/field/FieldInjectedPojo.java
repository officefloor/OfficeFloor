package net.officefloor.tutorial.dipojohttpserver.field;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.tutorial.dipojohttpserver.Pojo;

// START SNIPPET: tutorial
public class FieldInjectedPojo {

	private @Dependency Pojo pojo;

	public String getAudience() {
		return this.pojo.getAudience();
	}

}
// END SNIPPET: tutorial
