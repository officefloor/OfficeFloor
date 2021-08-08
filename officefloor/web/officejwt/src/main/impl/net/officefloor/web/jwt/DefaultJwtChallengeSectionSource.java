/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.source.PrivateSource;

/**
 * Default handling of JWT challenges.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultJwtChallengeSectionSource extends AbstractSectionSource {

	/*
	 * ==================== SectionSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Configure the empty function
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("FUNCTIONS",
				new EmptyManagedFunctionSource());
		SectionFunction retrieveJwtValidateKeys = namespace.addSectionFunction("function", "function");

		// Link inputs for JWT challenges
		designer.link(designer.addSectionInput(JwtHttpSecuritySource.Flows.NO_JWT.name(), null),
				retrieveJwtValidateKeys);
		designer.link(designer.addSectionInput(JwtHttpSecuritySource.Flows.INVALID_JWT.name(), null),
				retrieveJwtValidateKeys);
		designer.link(designer.addSectionInput(JwtHttpSecuritySource.Flows.EXPIRED_JWT.name(), null),
				retrieveJwtValidateKeys);
	}

	/**
	 * JWKS {@link ManagedFunctionSource}.
	 */
	@PrivateSource
	private static class EmptyManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ================== ManagedFunctionSource ================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Provide empty function
			functionNamespaceTypeBuilder.addManagedFunctionType("function", None.class, None.class)
					.setFunctionFactory(() -> (functionContext) -> {
					});
		}
	}

}
