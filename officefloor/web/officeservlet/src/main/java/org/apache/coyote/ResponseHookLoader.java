package org.apache.coyote;

/**
 * Loads the {@link ActionHook} to the {@link Response}.
 * 
 * @author Daniel Sagenschneider
 */
public class ResponseHookLoader {

	/**
	 * Loads the {@link ActionHook} to {@link Response}.
	 * 
	 * @param response {@link Response}.
	 * @param hook     {@link ActionHook}.
	 */
	public static void load(Response response, ActionHook hook) {
		response.setHook(hook);
	}

	/**
	 * All access via static methods.
	 */
	private ResponseHookLoader() {
	}
}