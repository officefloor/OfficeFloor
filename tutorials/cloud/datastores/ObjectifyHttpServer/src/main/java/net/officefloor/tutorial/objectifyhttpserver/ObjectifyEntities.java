package net.officefloor.tutorial.objectifyhttpserver;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Entity;

import net.officefloor.nosql.objectify.ObjectifyEntityLocator;

/**
 * {@link Objectify} {@link Entity} instances.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ObjectifyEntities implements ObjectifyEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}
// END SNIPPET: tutorial