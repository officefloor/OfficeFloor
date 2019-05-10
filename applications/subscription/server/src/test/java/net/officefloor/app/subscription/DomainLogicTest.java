package net.officefloor.app.subscription;

import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.nosql.objectify.mock.ObjectifyRule;
import net.officefloor.web.jwt.mock.MockJwtAccessTokenRule;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link DomainLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainLogicTest {

	private MockJwtAccessTokenRule jwt = new MockJwtAccessTokenRule();

	private ObjectifyRule obectify = new ObjectifyRule();

	private MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain chain = RuleChain.outerRule(this.jwt).around(this.obectify).around(this.server);

	@Test
	public void noDomains() {

//		fail("TODO implement");

	}

}