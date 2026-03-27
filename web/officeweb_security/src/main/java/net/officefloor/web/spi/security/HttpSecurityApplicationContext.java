/*-
 * #%L
 * Web Security
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
