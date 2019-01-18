package net.officefloor.web.spi.security;

import net.officefloor.frame.api.function.FlowCallback;

/**
 * Generic context for integrating {@link HttpSecurity} actions into the
 * application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityApplicationContext<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains a dependency.
	 * 
	 * @param key Key for the dependency.
	 * @return Dependency.
	 */
	Object getObject(O key);

	/**
	 * Undertakes a flow.
	 * 
	 * @param key       Key identifying the flow.
	 * @param parameter Parameter.
	 * @param callback  {@link FlowCallback}.
	 */
	void doFlow(F key, Object parameter, FlowCallback callback);

}