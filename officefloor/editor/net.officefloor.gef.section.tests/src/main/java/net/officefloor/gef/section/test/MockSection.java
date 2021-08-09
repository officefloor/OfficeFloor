/*-
 * #%L
 * net.officefloor.gef.section.tests
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

package net.officefloor.gef.section.test;

import net.officefloor.gef.section.SectionEditor;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Mock section {@link Class} for testing the {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSection {

	@FlowInterface
	public static interface Flows {

		void outputOne();

		void outputTwo(String value);
	}

	public void inputOne() {
	}

	public void inputTwo(MockObject object) {
		object.setValue("inputTwo");
	}

	public void inputThree(Flows flows) {
		flows.outputTwo("inputThree");
	}
}
