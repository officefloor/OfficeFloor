package org.apache.coyote;

/**
 * Loads {@link ActionHook} to {@link Request} and {@link Response}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActionHookLoader {

	/**
	 * Loads the {@link ActionHook} to the {@link Request} and {@link Response}.
	 * 
	 * @param actionHook {@link ActionHook}.
	 * @param request    {@link Request}.
	 * @param response   {@link Response}.
	 */
	public static void loadActionHook(ActionHook actionHook, Request request, Response response) {
		request.setHook(actionHook);
		response.setHook(actionHook);
	}

	/**
	 * Access via static method.
	 */
	private ActionHookLoader() {
	}
}