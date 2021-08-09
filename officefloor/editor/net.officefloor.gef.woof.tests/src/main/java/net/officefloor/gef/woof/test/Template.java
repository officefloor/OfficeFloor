/*-
 * #%L
 * net.officefloor.gef.woof.tests
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

package net.officefloor.gef.woof.test;

import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Example template logic.
 * 
 * @author Daniel Sagenschneider
 */
public class Template {

	@FlowInterface
	public static interface Flows {

		void flow();
	}
	
	public Template redirect() {
		return this;
	}

	public void getTemplate(Flows flows) {
	}
}
