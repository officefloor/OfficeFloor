/*-
 * #%L
 * Web on OfficeFloor Testing
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof.mock;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} for running the {@link MockWoofServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerRule extends MockWoofServer implements TestRule {

	/**
	 * {@link MockWoofServerConfigurer} instances.
	 */
	private final List<MockWoofServerConfigurer> configurers = new LinkedList<>();

	/**
	 * Additional profiles.
	 */
	private final List<String> profiles = new LinkedList<>();

	/**
	 * Override {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * Instantiate.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 */
	public MockWoofServerRule(MockWoofServerConfigurer... configurers) {
		this.configurers.addAll(Arrays.asList(configurers));

		// Allow configuring the profiles
		this.configurers.add((context, compiler) -> {

			// Add the profiles
			for (String profile : this.profiles) {
				context.addProfile(profile);
			}

			// Add the properties
			for (String name : this.properties.stringPropertyNames()) {
				String value = this.properties.getProperty(name);
				context.addOverrideProperty(name, value);
			}
		});
	}

	/**
	 * Builder pattern for adding an additional profile.
	 * 
	 * @param profile Additional profile.
	 * @return <code>this</code>.
	 */
	public MockWoofServerRule profile(String profile) {
		this.profiles.add(profile);
		return this;
	}

	/**
	 * Builder pattern for adding an override property.
	 * 
	 * @param name  Name.
	 * @param value Value.
	 * @return <code>this</code>.
	 */
	public MockWoofServerRule property(String name, String value) {
		this.properties.setProperty(name, value);
		return this;
	}

	/**
	 * =============== MockWoofServer =====================
	 */

	@Override
	public MockWoofServerRule timeout(int timeout) {
		super.timeout(timeout);
		return this;
	}

	/*
	 * =================== TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				MockWoofServerConfigurer[] config = MockWoofServerRule.this.configurers
						.toArray(new MockWoofServerConfigurer[MockWoofServerRule.this.configurers.size()]);
				try (MockWoofServer server = MockWoofServer.open(MockWoofServerRule.this, config)) {
					base.evaluate();
				}
			}
		};
	}

}
