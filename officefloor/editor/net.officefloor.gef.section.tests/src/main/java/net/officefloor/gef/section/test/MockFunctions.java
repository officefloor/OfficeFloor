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

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.gef.section.SectionEditor;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Functions for testing {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFunctions {

	@FlowInterface
	public static interface Flows {

		void outputOne();

		void outputTwo(String value);
	}

	public void functionOne() {
	}

	public void functionTwo(MockObject object, String parameter) {
		object.setValue("functionTwo");
	}

	public String functionThree(Flows flows) {
		flows.outputTwo("functionThree");
		return "functionThree";
	}

	public void functionFour() throws IOException, SQLException, RuntimeException {
	}
}
