/*-
 * #%L
 * JWT Security
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
