/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link SectionSource} to test overriding a {@link Property}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockPropertySectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link Property} that is being overridden.
	 */
	public static final String PROPERTY_NAME_OVERRIDE = "property.override";

	/**
	 * Name of {@link Property} that is being overridden by profile.
	 */
	public static final String PROFILE_OVERRIDE = "profile.override";

	/**
	 * Name of {@link Property} that is being overridden by test profile.
	 */
	public static final String TEST_PROFILE_OVERRIDE = "test.override";

	/*
	 * ===================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Add the servicing function
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("function",
				new MockPropertyFunctionSource());
		namespace.addProperty(PROPERTY_NAME_OVERRIDE, "property to be overridden");
		namespace.addProperty(PROFILE_OVERRIDE, "to be overridden by profile");
		namespace.addProperty(TEST_PROFILE_OVERRIDE, "to be overridden by test profile");
		SectionFunction function = namespace.addSectionFunction("function", "function");

		// Provide dependencies
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());
		designer.link(function.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), serverHttpConnection);

		// Link servicing
		SectionInput input = designer.addSectionInput("service", null);
		designer.link(input, function);
	}

	/**
	 * Dependency keys.
	 */
	private static enum Dependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link ManagedFunctionSource} to test overriding a {@link Property}.
	 */
	private static class MockPropertyFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * =============== ManagedFunctionSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_NAME_OVERRIDE, "Property verride");
			context.addProperty(PROFILE_OVERRIDE, "Profile Override");
			context.addProperty(TEST_PROFILE_OVERRIDE, "Test Profile Override");
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the values
			String propertyValue = context.getProperty(PROPERTY_NAME_OVERRIDE);
			String profileValue = context.getProperty(PROFILE_OVERRIDE);
			String testProfileValue = context.getProperty(TEST_PROFILE_OVERRIDE);

			// Provide function to respond with property value
			functionNamespaceTypeBuilder.addManagedFunctionType("function", Dependencies.class, None.class)
					.setFunctionFactory(() -> (mfContext) -> {
						ServerHttpConnection connection = (ServerHttpConnection) mfContext
								.getObject(Dependencies.SERVER_HTTP_CONNECTION);
						connection.getResponse().getEntityWriter()
								.write(propertyValue + ", " + profileValue + ", " + testProfileValue);
					}).addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}
