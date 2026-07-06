package net.officefloor.tutorial.objectifyhttpserver;

import net.officefloor.nosql.objectify.ObjectifyEntityLocator;

// START SNIPPET: tutorial
public class ObjectifyEntities implements ObjectifyEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial