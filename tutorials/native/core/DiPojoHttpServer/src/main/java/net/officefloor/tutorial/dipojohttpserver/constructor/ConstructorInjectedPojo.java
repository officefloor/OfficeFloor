package net.officefloor.tutorial.dipojohttpserver.constructor;

import net.officefloor.tutorial.dipojohttpserver.Pojo;

/**
 * Provides constructor injection.
 * 
 * @author Daniel Sagenshneider
 */
// START SNIPPET: tutorial
public class ConstructorInjectedPojo {

	private final Pojo pojo;

	/** Instantiate with injected {@link Pojo}. */
	public ConstructorInjectedPojo(Pojo pojo) {
		this.pojo = pojo;
	}

	/** Obtains the audience. */
	public String getAudience() {
		return this.pojo.getAudience();
	}

}
// END SNIPPET: tutorial
