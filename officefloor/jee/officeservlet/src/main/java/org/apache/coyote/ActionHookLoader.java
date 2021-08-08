/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
