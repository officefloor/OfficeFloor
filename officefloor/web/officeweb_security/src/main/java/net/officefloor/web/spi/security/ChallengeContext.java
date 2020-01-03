package net.officefloor.web.spi.security;

/**
 * Context for triggering a challenge.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChallengeContext<O extends Enum<O>, F extends Enum<F>>
		extends HttpSecurityActionContext, HttpSecurityApplicationContext<O, F>, HttpChallengeContext {
}