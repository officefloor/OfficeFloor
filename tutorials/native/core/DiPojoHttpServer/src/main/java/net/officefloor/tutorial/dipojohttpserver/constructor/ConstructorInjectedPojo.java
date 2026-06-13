package net.officefloor.tutorial.dipojohttpserver.constructor;

import net.officefloor.tutorial.dipojohttpserver.Pojo;

// START SNIPPET: tutorial
public class ConstructorInjectedPojo {

	private final Pojo pojo;

	public ConstructorInjectedPojo(Pojo pojo) {
		this.pojo = pojo;
	}

	public String getAudience() {
		return this.pojo.getAudience();
	}

}
// END SNIPPET: tutorial
