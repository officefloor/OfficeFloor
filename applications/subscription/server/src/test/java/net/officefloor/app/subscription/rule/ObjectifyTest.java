package net.officefloor.app.subscription.rule;

import org.junit.Rule;
import org.junit.Test;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import net.officefloor.app.subscription.store.Domain;

/**
 * Tests {@link Objectify}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyTest {

	@Rule
	public ObjectifyRule objectify = new ObjectifyRule(Domain.class);

	@Test
	public void objectify() {

		// Test
		Domain domain = new Domain("test.officefloor.net");
		System.out.println("Domain: " + domain.getId() + ": " + domain.getDomain());
		ObjectifyService.ofy().save().entities(domain).now();
		System.out.println("Domain: " + domain.getId() + ": " + domain.getDomain());
	}

}