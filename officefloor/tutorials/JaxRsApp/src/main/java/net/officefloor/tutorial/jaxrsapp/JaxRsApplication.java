package net.officefloor.tutorial.jaxrsapp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.asList(JaxRsResource.class));
	}

}