package net.officefloor.tutorial.googleappenginehttpserver;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.annotation.Entity;

import net.officefloor.nosql.objectify.ObjectifyEntityLocator;

/**
 * {@link Objectify} {@link Entity} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyEntities implements ObjectifyEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { Post.class };
	}

}