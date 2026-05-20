package net.officefloor.tutorial.dipojohttpserver.setter;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.tutorial.dipojohttpserver.Pojo;

/**
 * Provides setter injection.
 * 
 * @author Daniel Sagenshneider
 */
// START SNIPPET: tutorial
public class SetterInjectedPojo {

	private Pojo pojo;

	@Dependency
	public void setPojo(Pojo pojo) {
		this.pojo = pojo;
	}

	public String getAudience() {
		return this.pojo.getAudience();
	}

}
// END SNIPPET: tutorial
